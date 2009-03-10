package uk.ac.warwick.util.csv;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;

@SuppressWarnings("unchecked")
public final class CreateCSVDocumentTest extends MockObjectTestCase {
    
	public void testCreateCSVDocumentWriterIsCalled() throws IOException {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");
        String fieldWrapper = "\"";

        String rowAcolA = "rowAcolA";
        String rowAcolB = "rowAcolB";
        String rowBcolA = "rowBcolA";
        String rowBcolB = "rowBcolB";

        Object rowA = new Object();
        Object rowB = new Object();

        Mock mockWriter = mock(CSVLineWriter.class);
        mockWriter.expects(once()).method("getNoOfColumns").with(eq(rowA)).will(returnValue(2));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(0)).will(returnValue(rowAcolA));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(1)).will(returnValue(rowAcolB));
        mockWriter.expects(once()).method("getNoOfColumns").with(eq(rowB)).will(returnValue(2));
        mockWriter.expects(once()).method("getColumn").with(eq(rowB), eq(0)).will(returnValue(rowBcolA));
        mockWriter.expects(once()).method("getColumn").with(eq(rowB), eq(1)).will(returnValue(rowBcolB));

        StringWriter writer = new StringWriter();
        CSVDocument<Object> document = new CSVDocument<Object>((CSVLineWriter<Object>) mockWriter.proxy(), null);
        document.addLine(rowA);
        document.addLine(rowB);
        document.write(writer);

        String s = writer.toString();
        String expectedString =  fieldWrapper + rowAcolA + fieldWrapper + discriminator + fieldWrapper + rowAcolB + fieldWrapper + lineSeperator;
        expectedString +=  fieldWrapper + rowBcolA + fieldWrapper + discriminator + fieldWrapper + rowBcolB + fieldWrapper + lineSeperator;
        assertEquals(expectedString, s);
    }

    public void testCreateCSVDocumentWriterHandlesNull() throws IOException {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");
        String fieldWrapper = "\"";

        String rowAcolA = "rowAcolA";
        String rowAcolB = null;

        Object rowA = new Object();

        Mock mockWriter = mock(CSVLineWriter.class);
        mockWriter.expects(once()).method("getNoOfColumns").with(eq(rowA)).will(returnValue(2));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(0)).will(returnValue(rowAcolA));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(1)).will(returnValue(rowAcolB));

        StringWriter writer = new StringWriter();
        CSVDocument document = new CSVDocument((CSVLineWriter) mockWriter.proxy(), null);
        document.addLine(rowA);
        document.write(writer);

        String s = writer.toString();
        String expectedString =  fieldWrapper + rowAcolA + fieldWrapper + discriminator + fieldWrapper + "" + fieldWrapper + lineSeperator;
        assertEquals(expectedString, s);
    }

    public void testCreateCSVDocumentDiscriminatorsAreEscaped() throws IOException {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");
        String fieldWrapper = "\"";
        String rowAcolA = "rowA" + discriminator + "colA";
        String rowAcolB = "rowAcolB";

        Object rowA = new Object();

        Mock mockWriter = mock(CSVLineWriter.class);
        mockWriter.expects(once()).method("getNoOfColumns").will(returnValue(2));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(0)).will(returnValue(rowAcolA));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(1)).will(returnValue(rowAcolB));

        StringWriter writer = new StringWriter();
        CSVDocument document = new CSVDocument((CSVLineWriter) mockWriter.proxy(), null);
        document.addLine(rowA);
        document.write(writer);

        String s = writer.toString();
        String expectedString =  fieldWrapper + rowAcolA + fieldWrapper + discriminator + fieldWrapper + rowAcolB + fieldWrapper + lineSeperator;
        assertEquals(expectedString, s);
    }

    public void testReadCSVDocumentReaderIsCalled() throws Exception {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");

        String rowAcolA = "rowAcolA";
        String rowAcolB = "rowAcolB";
        String rowBcolA = "rowBcolA";
        String rowBcolB = "rowBcolB";

        Object rowA = new Object();
        Object rowB = new Object();

        Mock mockReader = mock(CSVLineReader.class);
        mockReader.expects(once()).method("constructNewObject").will(returnValue(rowB));
        // for some reason mockReader fails if we define rowA first.  Hmmm?
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowB), eq(0), eq(rowBcolA)
        });
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowB), eq(1), eq(rowBcolB)
        });
        mockReader.expects(once()).method("end").with(eq(rowB));

        mockReader.expects(once()).method("constructNewObject").will(returnValue(rowA));
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowA), eq(0), eq(rowAcolA)
        });
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowA), eq(1), eq(rowAcolB)
        });
        mockReader.expects(once()).method("end").with(eq(rowA));
        mockReader.expects(atLeastOnce()).method("endData");

        String source = rowAcolA + discriminator + rowAcolB + lineSeperator;
        source +=  rowBcolA + discriminator + rowBcolB + lineSeperator;
        StringReader reader = new StringReader(source);

        CSVDocument document = new CSVDocument(null, (CSVLineReader) mockReader.proxy());
        document.read(reader);
        assertEquals("number of rows", 2, document.getNumberOfRows());
        assertEquals("first row", rowA, document.getRow(0));
        assertEquals("second row", rowB, document.getRow(1));
    }

    public void testReadCSVDocumentDiscriminatorsAreEscaped() throws Exception {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");
        String fieldWrapper = "\"";

        String rowAcolA = "rowAcolA";
        String rowAcolB = "rowAcolB";

        Object rowA = new Object();

        Mock mockReader = mock(CSVLineReader.class);
        mockReader.expects(once()).method("constructNewObject").will(returnValue(rowA));
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
            eq(rowA), eq(0), eq(rowAcolA)
        });
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowA), eq(1), eq(rowAcolB)
        });
        mockReader.expects(once()).method("end").with(eq(rowA));
        mockReader.expects(once()).method("endData");

        String source = fieldWrapper + rowAcolA + fieldWrapper + discriminator + rowAcolB + lineSeperator;
        StringReader reader = new StringReader(source);

        CSVDocument document = new CSVDocument(null, (CSVLineReader) mockReader.proxy());
        document.read(reader);
        assertEquals("number of rows", 1, document.getNumberOfRows());
        assertEquals("first row", rowA, document.getRow(0));
    }

    public void testReadCSVDocumentCanHandleNull() throws Exception {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");
        String fieldWrapper = "\"";

        String rowAcolA = "rowAcolA";
        String rowAcolB = "";
        String rowAcolC = "rowAcolC";

        Object rowA = new Object();

        Mock mockReader = mock(CSVLineReader.class);
        mockReader.expects(once()).method("constructNewObject").will(returnValue(rowA));
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
            eq(rowA), eq(0), eq(rowAcolA)
        });
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowA), eq(1), eq(rowAcolB)
        });
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowA), eq(2), eq(rowAcolC)
        });
        mockReader.expects(once()).method("end").with(eq(rowA));
        mockReader.expects(once()).method("endData");

        String source = fieldWrapper + rowAcolA + fieldWrapper + discriminator + fieldWrapper + rowAcolB + fieldWrapper + discriminator + fieldWrapper + rowAcolC + fieldWrapper + lineSeperator;
        StringReader reader = new StringReader(source);

        CSVDocument document = new CSVDocument(null, (CSVLineReader) mockReader.proxy());
        document.read(reader);
        assertEquals("number of rows", 1, document.getNumberOfRows());
        assertEquals("first row", rowA, document.getRow(0));
    }
    
    public void testGoodCreateCSVDocumentWriterIsCalled() throws Exception {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");
        String fieldWrapper = "\"";

        String rowAcolA = "rowAcolA";
        String rowAcolB = "rowAcolB";
        String rowBcolA = "rowBcolA";
        String rowBcolB = "rowBcolB";

        Object rowA = new Object();
        Object rowB = new Object();

        Mock mockWriter = mock(CSVLineWriter.class);
        mockWriter.expects(once()).method("getNoOfColumns").with(eq(rowA)).will(returnValue(2));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(0)).will(returnValue(rowAcolA));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(1)).will(returnValue(rowAcolB));
        mockWriter.expects(once()).method("getNoOfColumns").with(eq(rowB)).will(returnValue(2));
        mockWriter.expects(once()).method("getColumn").with(eq(rowB), eq(0)).will(returnValue(rowBcolA));
        mockWriter.expects(once()).method("getColumn").with(eq(rowB), eq(1)).will(returnValue(rowBcolB));

        StringWriter writer = new StringWriter();
        GoodCsvDocument document = new GoodCsvDocument((CSVLineWriter) mockWriter.proxy(), null);
        document.addLine(rowA);
        document.addLine(rowB);
        document.write(writer);

        String s = writer.toString();
        String expectedString =  fieldWrapper + rowAcolA + fieldWrapper + discriminator + fieldWrapper + rowAcolB + fieldWrapper + lineSeperator;
        expectedString +=  fieldWrapper + rowBcolA + fieldWrapper + discriminator + fieldWrapper + rowBcolB + fieldWrapper + lineSeperator;
        assertEquals(expectedString, s);
    }

    public void testGoodCreateCSVDocumentWriterHandlesNull() throws Exception {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");
        String fieldWrapper = "\"";

        String rowAcolA = "rowAcolA";
        String rowAcolB = null;

        Object rowA = new Object();

        Mock mockWriter = mock(CSVLineWriter.class);
        mockWriter.expects(once()).method("getNoOfColumns").with(eq(rowA)).will(returnValue(2));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(0)).will(returnValue(rowAcolA));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(1)).will(returnValue(rowAcolB));

        StringWriter writer = new StringWriter();
        GoodCsvDocument document = new GoodCsvDocument((CSVLineWriter) mockWriter.proxy(), null);
        document.addLine(rowA);
        document.write(writer);

        String s = writer.toString();
        String expectedString =  fieldWrapper + rowAcolA + fieldWrapper + discriminator + fieldWrapper + "" + fieldWrapper + lineSeperator;
        assertEquals(expectedString, s);
    }

    public void testGoodCreateCSVDocumentDiscriminatorsAreEscaped() throws Exception {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");
        String fieldWrapper = "\"";
        String rowAcolA = "rowA" + discriminator + "colA";
        String rowAcolB = "rowAcolB";

        Object rowA = new Object();

        Mock mockWriter = mock(CSVLineWriter.class);
        mockWriter.expects(once()).method("getNoOfColumns").will(returnValue(2));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(0)).will(returnValue(rowAcolA));
        mockWriter.expects(once()).method("getColumn").with(eq(rowA), eq(1)).will(returnValue(rowAcolB));

        StringWriter writer = new StringWriter();
        GoodCsvDocument document = new GoodCsvDocument((CSVLineWriter) mockWriter.proxy(), null);
        document.addLine(rowA);
        document.write(writer);

        String s = writer.toString();
        String expectedString =  fieldWrapper + rowAcolA + fieldWrapper + discriminator + fieldWrapper + rowAcolB + fieldWrapper + lineSeperator;
        assertEquals(expectedString, s);
    }

    public void testGoodReadCSVDocumentReaderIsCalled() throws Exception {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");

        String rowAcolA = "rowAcolA";
        String rowAcolB = "rowAcolB";
        String rowBcolA = "rowBcolA";
        String rowBcolB = "rowBcolB";

        Object rowA = new Object();
        Object rowB = new Object();

        Mock mockReader = mock(CSVLineReader.class);
        mockReader.expects(once()).method("constructNewObject").will(returnValue(rowB));
        // for some reason mockReader fails if we define rowA first.  Hmmm?
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowB), eq(0), eq(rowBcolA)
        });
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowB), eq(1), eq(rowBcolB)
        });
        mockReader.expects(once()).method("end").with(eq(rowB));

        mockReader.expects(once()).method("constructNewObject").will(returnValue(rowA));
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowA), eq(0), eq(rowAcolA)
        });
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowA), eq(1), eq(rowAcolB)
        });
        mockReader.expects(once()).method("end").with(eq(rowA));
        mockReader.expects(atLeastOnce()).method("endData");

        String source = rowAcolA + discriminator + rowAcolB + lineSeperator;
        source +=  rowBcolA + discriminator + rowBcolB + lineSeperator;
        StringReader reader = new StringReader(source);

        GoodCsvDocument document = new GoodCsvDocument(null, (CSVLineReader) mockReader.proxy());
        document.read(reader);
        assertEquals("number of rows", 2, document.getNumberOfRows());
        assertEquals("first row", rowA, document.getRow(0));
        assertEquals("second row", rowB, document.getRow(1));
    }

    public void testGoodReadCSVDocumentDiscriminatorsAreEscaped() throws Exception {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");
        String fieldWrapper = "\"";

        String rowAcolA = "rowAcolA";
        String rowAcolB = "rowAcolB";

        Object rowA = new Object();

        Mock mockReader = mock(CSVLineReader.class);
        mockReader.expects(once()).method("constructNewObject").will(returnValue(rowA));
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
            eq(rowA), eq(0), eq(rowAcolA)
        });
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowA), eq(1), eq(rowAcolB)
        });
        mockReader.expects(once()).method("end").with(eq(rowA));
        mockReader.expects(once()).method("endData");

        String source = fieldWrapper + rowAcolA + fieldWrapper + discriminator + rowAcolB + lineSeperator;
        StringReader reader = new StringReader(source);

        GoodCsvDocument document = new GoodCsvDocument(null, (CSVLineReader) mockReader.proxy());
        document.read(reader);
        assertEquals("number of rows", 1, document.getNumberOfRows());
        assertEquals("first row", rowA, document.getRow(0));
    }

    public void testReadGoodCSVDocumentCanHandleNull() throws Exception {
        String discriminator = ",";
        String lineSeperator = System.getProperty("line.separator");
        String fieldWrapper = "\"";

        String rowAcolA = "rowAcolA";
        String rowAcolB = "";
        String rowAcolC = "rowAcolC";

        Object rowA = new Object();

        Mock mockReader = mock(CSVLineReader.class);
        mockReader.expects(once()).method("constructNewObject").will(returnValue(rowA));
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
            eq(rowA), eq(0), eq(rowAcolA)
        });
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowA), eq(1), eq(rowAcolB)
        });
        mockReader.expects(once()).method("setColumn").with(new Constraint[] {
                eq(rowA), eq(2), eq(rowAcolC)
        });
        mockReader.expects(once()).method("end").with(eq(rowA));
        mockReader.expects(once()).method("endData");

        String source = fieldWrapper + rowAcolA + fieldWrapper + discriminator + fieldWrapper + rowAcolB + fieldWrapper + discriminator + fieldWrapper + rowAcolC + fieldWrapper + lineSeperator;
        StringReader reader = new StringReader(source);

        GoodCsvDocument document = new GoodCsvDocument(null, (CSVLineReader) mockReader.proxy());
        document.read(reader);
        assertEquals("number of rows", 1, document.getNumberOfRows());
        assertEquals("first row", rowA, document.getRow(0));
    }
}
