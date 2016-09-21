/* Small scripts that converts jQuery.IME rules files into XML files  
 *
 * I understand that `eval` is evil. But in this case, that's ok. 
 * This is a build script, notihng more. And it is `eval`ing code
 * that is from the wikimedia foundation. I expect that nothing
 * horribly dangerous to go through :) */
var fs = require('fs');
var xmlbuilder = require('xmlbuilder');
var _ = require('underscore');
var path = require('path');

/* TODO: Replace with proper options handling */
var inputFile = process.argv[2];
var outputFile = process.argv[3];

function convertFixture( fixture, doc ) {
    var fixtureXML = doc.ele( 'fixture', { 
        description: fixture.description,
        inputmethod: fixture.inputmethod,
        multiline: fixture.multiline === true
    } );
    _.each( fixture.tests, function( test, i ) {
        if( _.isArray( test.input ) ) {
            var text = "";
            var altGr = "";
            _.each( test.input, function( key ) {
                text += key[ 0 ];
                altGr += key[ 1 ] ? 1 : 0;
            } );
            test.input = text;
            test.altGr = altGr;
        }
        fixtureXML.ele( 'test', test );
    } );
}


fs.readFile( inputFile, 'utf8', function( err, data ) {
    eval( data );
    var xml = xmlbuilder.create( 'fixtures' );
    _.each( testFixtures, function( fixture, i ) {
        convertFixture( fixture, xml );
    } );
    var xmlString =xml.end( { pretty: true } );
    fs.writeFile( outputFile, xmlString, function( err ) {
        if( err ) {
            console.log( err );
        }
    } );
} );
