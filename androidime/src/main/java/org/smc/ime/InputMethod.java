package com.mobiletin.ime;

import android.content.Context;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class InputMethod {
    static class InputPattern {
        private Pattern inputPattern;
        private Pattern contextPattern;
        private String replacement;
        private Boolean altGr;

        public InputPattern(String input, String context, String replacement, Boolean altGr) {
            this.inputPattern = Pattern.compile(input + "$");
            if (context != null) {
                this.contextPattern = Pattern.compile(context + "$");
            } else {
                this.contextPattern = null;
            }
            this.replacement = replacement;
            this.altGr = altGr;
        }

        public InputPattern(String input, String replacement, Boolean altGr) {
            this(input, null, replacement, altGr);
        }

        public InputPattern(String input, String context, String replacement) {
            this(input, context, replacement, false);
        }

        public InputPattern(String input, String replacement) {
            this(input, null, replacement, false);
        }
    }

    private String id;
    private String name;
    private String description;
    private String author;
    private String version;
    private int maxKeyLength;
    private int contextLength;

    private ArrayList<InputPattern> patterns;

    public InputMethod(String id, String name, String description, String author, String version, int contextLenght, int maxKeyLength, ArrayList<InputPattern> patterns) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.author = author;
        this.version = version;
        this.patterns = patterns;
        this.maxKeyLength = maxKeyLength;
        this.contextLength = contextLenght;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public int getContextLength() {
        return contextLength;
    }

    public int getMaxKeyLength() {
        return maxKeyLength;
    }

    // Implementation of Matcher.replaceAll differs in Android and JDK
    // In JDK, if a group has not been captured, it is replaced with ""
    // In android, it is replaced with the string 'null'
    // This is annoying. Can't really be fixed without fully encapsulating the class
    // So, rewriting replaceAll algorithm
    // So we'll just do backref replacement and pass it back to the original replaceAll
    private String replaceAll(Matcher matcher, String input, String replacement) {
        // Process substitution string to replace group references with groups
        int cursor = 0;
        StringBuilder result = new StringBuilder();

        while (cursor < replacement.length()) {
            char nextChar = replacement.charAt(cursor);
            if (nextChar == '\\') {
                cursor++;
                nextChar = replacement.charAt(cursor);
                result.append(nextChar);
                cursor++;
            } else if (nextChar == '$') {
                // Skip past $
                cursor++;
                // The first number is always a group
                int refNum = (int) replacement.charAt(cursor) - '0';
                if ((refNum < 0) || (refNum > 9))
                    throw new IllegalArgumentException(
                            "Illegal group reference");
                cursor++;

                // Capture the largest legal group string
                boolean done = false;
                while (!done) {
                    if (cursor >= replacement.length()) {
                        break;
                    }
                    int nextDigit = replacement.charAt(cursor) - '0';
                    if ((nextDigit < 0) || (nextDigit > 9)) { // not a number
                        break;
                    }
                    int newRefNum = (refNum * 10) + nextDigit;
                    if (matcher.groupCount() < newRefNum) {
                        done = true;
                    } else {
                        refNum = newRefNum;
                        cursor++;
                    }
                }
                // Append group
                if (matcher.group(refNum) != null)
                    result.append(matcher.group(refNum));
            } else {
                result.append(nextChar);
                cursor++;
            }
        }

        return matcher.replaceAll(result.toString());
    }

    public String transliterate(String input, String context, Boolean altGr) {
        for (InputPattern pattern : patterns) {
            Matcher inputMatcher = pattern.inputPattern.matcher(input);
            if (inputMatcher.find()) {
                if (pattern.contextPattern == null || pattern.contextPattern.matcher(context).find()) {
                    if (pattern.altGr == altGr) {
                        return replaceAll(inputMatcher, input, pattern.replacement);
                    }
                }
            }
        }
        return input;
    }

    public static int firstDivergence(String str1, String str2) {
        int length = str1.length() > str2.length() ? str2.length() : str1.length();
        for (int i = 0; i < length; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                return i;
            }
        }
        return length - 1; // Default
    }

    public String transliterateAll(String input, ArrayList<Boolean> altGr) {
        String curOutput = "";
        String replacement;
        String context = "";
        boolean curAltGr = false;
        for (int i = 0; i < input.length(); i++) {
            String c = String.valueOf(input.charAt(i));
            int startPos = curOutput.length() > this.getMaxKeyLength() ? curOutput.length() - this.getMaxKeyLength() : 0;
            String toReplace = curOutput.substring(startPos) + c;
            curAltGr = altGr != null && altGr.size() > i && altGr.get(i);
            replacement = this.transliterate(toReplace, context, curAltGr);
            int divIndex = InputMethod.firstDivergence(toReplace, replacement);
            replacement = replacement.substring(divIndex);
            curOutput = curOutput.substring(0, startPos + divIndex) + replacement;

            context += c;
            if (context.length() > this.getContextLength()) {
                context = context.substring(context.length() - this.getContextLength());
            }
        }
        return curOutput;
    }

    public static InputMethod fromName(String name, Context context) throws IllegalArgumentException {
        Log.e("Key Board Name", name);
        if(mapKeyBoard.get(name) == null) {
            throw new IllegalArgumentException("No such input method exists!");
        }

        InputStream stream = context.getResources().openRawResource(mapKeyBoard.get(name));
        //InputMethod.class.getClassLoader().getResourceAsStream("org/wikimedia/morelangs/res/" + name + ".xml");
        return fromFile(stream);
    }

    public static InputMethod fromFile(InputStream input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("No such input method exists!");
        }
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
        } catch (SAXException e) {
            // Since this shouldn't happen, let me semi-swallow this into a runtime exception
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            // Since this shouldn't happen, let me semi-swallow this into a runtime exception
            throw new RuntimeException(e);
        } catch (IOException e) {
            // Since this shouldn't happen, let me semi-swallow this into a runtime exception
            throw new RuntimeException(e);
        }
        Node root = doc.getDocumentElement();
        String id = root.getAttributes().getNamedItem("id").getTextContent();
        String name = root.getAttributes().getNamedItem("name").getTextContent();
        String description = root.getAttributes().getNamedItem("description").getTextContent();
        String author = root.getAttributes().getNamedItem("author").getTextContent();
        String version = root.getAttributes().getNamedItem("version").getTextContent();
        int maxKeyLength = Integer.parseInt(root.getAttributes().getNamedItem("maxKeyLength").getTextContent());
        int contextLength = Integer.parseInt(root.getAttributes().getNamedItem("contextLength").getTextContent());

        ArrayList<InputPattern> patterns = new ArrayList<InputMethod.InputPattern>();
        NodeList patternsXML = doc.getElementsByTagName("pattern");
        for (int i = 0; i < patternsXML.getLength(); i++) {
            NamedNodeMap attribs = patternsXML.item(i).getAttributes();
            InputPattern p;
            String inputPattern = attribs.getNamedItem("input").getTextContent();
            String replacement = attribs.getNamedItem("replacement").getTextContent();
            String context = null;
            Boolean altGr = false;
            if (attribs.getNamedItem("context") != null) {
                context = attribs.getNamedItem("context").getTextContent();
            }
            if (attribs.getNamedItem("altGr") != null) {
                altGr = attribs.getNamedItem("altGr").getTextContent().equals("true");
            }
            p = new InputPattern(inputPattern, context, replacement, altGr);
            patterns.add(p);
        }
        return new InputMethod(id, name, description, author, version, contextLength, maxKeyLength, patterns);
    }

    private static final HashMap<String, Integer> mapKeyBoard = new HashMap<String, Integer>() {{
        put("am-transliteration", R.raw.am_transliteration);
        put("ar-kbd", R.raw.ar_kbd);
        put("as-avro", R.raw.as_avro);
        put("as-bornona", R.raw.as_bornona);
        put("as-inscript", R.raw.as_inscript);
        put("as-inscript2", R.raw.as_inscript2);
        put("as-phonetic", R.raw.as_phonetic);
        put("as-transliteration", R.raw.as_transliteration);
        put("be-kbd", R.raw.be_kbd);
        put("be-latin", R.raw.be_latin);
        put("be-transliteration", R.raw.be_transliteration);
        put("ber-tfng", R.raw.ber_tfng);
        put("bn-avro", R.raw.bn_avro);
        put("bn-inscript", R.raw.bn_inscript);
        put("bn-inscript2", R.raw.bn_inscript2);
        put("bn-nkb", R.raw.bn_nkb);
        put("bn-probhat", R.raw.bn_probhat);
        put("brx-inscript", R.raw.brx_inscript);
        put("cyrl-palochka", R.raw.cyrl_palochka);
        put("da-normforms", R.raw.da_normforms);
        put("de", R.raw.de);
        put("el-kbd", R.raw.el_kbd);
        put("eo-h-f", R.raw.eo_h_f);
        put("eo-h", R.raw.eo_h);
        put("eo-plena", R.raw.eo_plena);
        put("eo-q", R.raw.eo_q);
        put("eo-transliteration", R.raw.eo_transliteration);
        put("eo-vi", R.raw.eo_vi);
        put("eo-x", R.raw.eo_x);
        put("fi-transliteration", R.raw.fi_transliteration);
        put("fo-normforms", R.raw.fo_normforms);
        put("gu-inscript", R.raw.gu_inscript);
        put("gu-inscript2", R.raw.gu_inscript2);
        put("gu-phonetic", R.raw.gu_phonetic);
        put("gu-transliteration", R.raw.gu_transliteration);
        put("he-kbd", R.raw.he_kbd);
        put("he-standard-2012-extonly", R.raw.he_standard_2012_extonly);
        put("he-standard-2012", R.raw.he_standard_2012);
        put("hi-bolnagri", R.raw.hi_bolnagri);
        put("hi-inscript", R.raw.hi_inscript);
        put("hi-phonetic", R.raw.hi_phonetic);
        put("hi-transliteration", R.raw.hi_transliteration);
        put("hr-kbd", R.raw.hr_kbd);
        put("hy-kbd", R.raw.hy_kbd);
        put("ipa-sil", R.raw.ipa_sil);
        put("is-normforms", R.raw.is_normforms);
        put("jv-transliteration", R.raw.jv_transliteration);
        put("ka-kbd", R.raw.ka_kbd);
        put("ka-transliteration", R.raw.ka_transliteration);
        put("kk-arabic", R.raw.kk_arabic);
        put("kk-kbd", R.raw.kk_kbd);
        put("kn-inscript", R.raw.kn_inscript);
        put("kn-inscript2", R.raw.kn_inscript2);
        put("kn-kgp", R.raw.kn_kgp);
        put("kn-transliteration", R.raw.kn_transliteration);
        put("kok-inscript2", R.raw.kok_inscript2);
        put("ks-Kbd", R.raw.ks_kbd);
        put("ks-inscript", R.raw.ks_inscript);
        put("lo-kbd", R.raw.lo_kbd);
        put("mai-inscript", R.raw.mai_inscript);
        put("ml-inscript", R.raw.ml_inscript);
        put("ml-inscript2", R.raw.ml_inscript2);
        put("ml-transliteration", R.raw.ml_transliteration);
        put("ml-swanalekha", R.raw.ml_swanalekha);
        put("mn-cyrl", R.raw.mn_cyrl);
        put("mr-inscript", R.raw.mr_inscript);
        put("mr-inscript2", R.raw.mr_inscript2);
        put("mr-phonetic", R.raw.mr_phonetic);
        put("mr-transliteration", R.raw.mr_transliteration);
        put("my-kbd", R.raw.my_kbd);
        put("ne-inscript", R.raw.ne_inscript);
        put("ne-inscript2", R.raw.ne_inscript2);
        put("ne-transliteration", R.raw.ne_transliteration);
        put("no-normforms", R.raw.no_normforms);
        put("no-tildeforms", R.raw.no_tildeforms);
        put("or-inscript", R.raw.or_inscript);
        put("or-inscript2", R.raw.or_inscript2);
        put("or-lekhani", R.raw.or_lekhani);
        put("or-phonetic", R.raw.or_phonetic);
        put("or-transliteration", R.raw.or_transliteration);
        put("pa-inscript", R.raw.pa_inscript);
        put("pa-inscript2", R.raw.pa_inscript2);
        put("pa-jhelum", R.raw.pa_jhelum);
        put("pa-phonetic", R.raw.pa_phonetic);
        put("pa-transliteration", R.raw.pa_transliteration);
        put("reverse-ta-transliteration", R.raw.reverse_ta_transliteration);
        put("ru-jcuken", R.raw.ru_jcuken);
        put("ru-kbd", R.raw.ru_kbd);
        put("sa-inscript", R.raw.sa_inscript);
        put("sa-inscript2", R.raw.sa_inscript2);
        put("sa-transliteration", R.raw.sa_transliteration);
        put("sah-transliteration", R.raw.sah_transliteration);
        put("se-normforms", R.raw.se_normforms);
        put("si-singlish", R.raw.si_singlish);
        put("si-wijesekara", R.raw.si_wijesekara);
        put("sk-kbd", R.raw.sk_kbd);
        put("sr-kbd", R.raw.sr_kbd);
        put("sv-normforms", R.raw.sv_normforms);
        put("ta-99", R.raw.ta_99);
        put("ta-bamini", R.raw.ta_bamini);
        put("ta-inscript", R.raw.ta_inscript);
        put("ta-inscript2", R.raw.ta_inscript2);
        put("ta-transliteration", R.raw.ta_transliteration);
        put("te-inscript", R.raw.te_inscript);
        put("te-inscript2", R.raw.te_inscript2);
        put("te-kachatathapa", R.raw.te_kachatathapa);
        put("te-transliteration", R.raw.te_transliteration);
        put("th-kedmanee", R.raw.th_kedmanee);
        put("th-pattachote", R.raw.th_pattachote);
        put("transliteration", R.raw.transliteration);
        put("ua-kbd", R.raw.ua_kbd);
        put("ug-kbd", R.raw.ug_kbd);
        put("ur-transliteration", R.raw.ur_transliteration);
        put("uz-kbd", R.raw.uz_kbd);
    }};
}    
