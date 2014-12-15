/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.re.css;

import be.re.xml.Accumulator;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.InputSource;

/**
 *
 * @author ggeurts
 */
public class CSSToXSLFOConverterTest
{
    private final URL baseUrl = getClass().getResource(getClass().getSimpleName() + ".class");

    private static CSSToXSLFOConverter cssConverter;
    
    @BeforeClass
    public static void setUpClass() throws Exception
    {
        HashMap prefixMap = new HashMap();
        prefixMap.put("fo", Constants.XSLFO);
        XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
        
        cssConverter = new CSSToXSLFOConverter(null);
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    @Test
    public void hasDefaultCatalog() throws Exception
    {
        assertNotNull("getCatalogResolver()", cssConverter.getCatalogResolver());
    }

    @Test
    public void hasDefaultCssResolver() throws Exception
    {
        assertNotNull("getCssResolver()", cssConverter.getCssResolver());
    }

    /**
     * Test of convert method, of class CSSToXSLFOConverter.
     */
    @Test
    public void canConvertXhtml() throws Exception
    {
        String xhtml = "<html xmlns='http://www.w3.org/1999/xhtml'>\n" +
            "<head>\n" +
            "<title>Test document</title>\n" +
            "</head>\n" +
            "<body><h1>Hello world</h1></body>\n" +
            "</html>";
        InputSource source = new InputSource(new StringReader(xhtml));

        Accumulator out = TestUtil.createAccumulator();
        cssConverter.convert(source, out, baseUrl, null, null, null, null);

        String xslfo = TestUtil.toXmlString(out);
        XMLAssert.assertXpathExists("/fo:root", out.getDocument());
    }

    /**
     * Test of convert method, of class CSSToXSLFOConverter.
     */
    @Test
    public void canConvertXhtmlWithTextBodyOnly() throws Exception
    {
        String xhtml = "<html xmlns='http://www.w3.org/1999/xhtml'>\n" +
            "<head>\n" +
            "<title>Test document</title>\n" +
            "</head>\n" +
            "<body>Hello world</body>\n" +
            "</html>";
        InputSource source = new InputSource(new StringReader(xhtml));

        Accumulator out = TestUtil.createAccumulator();
        cssConverter.convert(source, out, baseUrl, null, null, null, null);

        XMLAssert.assertXpathExists("/fo:root", out.getDocument());
    }
}
