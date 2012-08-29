package org.emg.adf.tftester.rt.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import javax.xml.bind.Unmarshaller;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import javax.xml.validation.SchemaFactory;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.JboException;

import org.emg.adf.tftester.rt.model.xml.jaxb.ObjectFactory;
import org.emg.adf.tftester.rt.model.xml.jaxb.ParamValueObjectType;
import org.emg.adf.tftester.rt.model.xml.jaxb.TaskFlowTesterType;
import org.emg.adf.tftester.rt.model.xml.jaxb.TaskFlowType;
import org.emg.adf.tftester.rt.model.xml.jaxb.TestCaseType;

public class TaskFlowTesterService
{
  private List<TaskFlow> testTaskFlows = new ArrayList<TaskFlow>();
  ADFLogger sLog = ADFLogger.createADFLogger(TaskFlowTesterService.class);

  public TaskFlowTesterService()
  {
    super();
    loadDefaultTestcases();
  }

  public void setTestTaskFlows(List<TaskFlow> testTaskFlows)
  {
    this.testTaskFlows = testTaskFlows;
  }

  public List<TaskFlow> getTestTaskFlows()
  {
    return testTaskFlows;
  }

  public String exportToXML()
  {
    return writeToXML(createJaxbModelFromBeanModel());
  }

  public void importFromXml(String xml)
  {
    TaskFlowTesterType tftester = createJaxbModelFromXml(xml);
    createBeanModelFromJaxbModel(tftester);
    // create bean model from jaxbmodel
  }

  private void createBeanModelFromJaxbModel(TaskFlowTesterType tftester)
  {
    for (Object jaxbTfObject: tftester.getTaskFlow())
    {
      TaskFlowType jaxbTf = (TaskFlowType) jaxbTfObject;
      TaskFlow tf = new TaskFlow();
      tf.setDisplayName(jaxbTf.getDisplayName());
      tf.setTaskFlowIdString(jaxbTf.getTaskFlowId());
      getTestTaskFlows().add(tf);
      // loop over TF testcases
      for (Object jaxbTcObject: jaxbTf.getTestCase())
      {
        TestCaseType jaxbTc = (TestCaseType) jaxbTcObject;
        TaskFlowTestCase tc = new TaskFlowTestCase(tf);
        tf.addTestCase(tc);
        tc.setDescription(jaxbTc.getDescription());
        tc.setName(jaxbTc.getName());
        tc.setRunAscall(jaxbTc.isRunAscall());
        tc.setRunInRegion(jaxbTc.isRunInRegion());
        tc.setStretchLayout(jaxbTc.isStretchLayout());
        // loop over param values
        if (jaxbTc.getParamValueObject() != null)
        {
          for (Object jaxbVoObject: jaxbTc.getParamValueObject())
          {
            ParamValueObjectType jaxbVo = (ParamValueObjectType) jaxbVoObject;
            ValueObject vo = new ValueObject(jaxbVo.getName(), jaxbVo.getClassName(), null, false);
            tc.getParamValueObjects().add(vo);
            String value = jaxbVo.getValueAsString();
            if (value != null && !"".equals(value))
            {
              vo.setValueAsString(value);
              if (value.startsWith("#{"))
              {
                vo.setElExpressionUsed(true);
              }
            }
            // add  detail param values
            addNestedValueObjects(vo, jaxbVo);
          }
        }
      }
    }
  }

  private void addNestedValueObjects(ValueObject vo, ParamValueObjectType jaxbVo)
  {
    if (jaxbVo.getParamValueObject() != null)
    {
      for (Object jaxbDetailVoObject: jaxbVo.getParamValueObject())
      {
        ParamValueObjectType jaxbDetailVo = (ParamValueObjectType) jaxbDetailVoObject;
        ValueObject detailVo =
          new ValueObject(jaxbDetailVo.getName(), jaxbDetailVo.getClassName(), vo, vo.isMapType());
        vo.getValueProperties().add(detailVo);
        String value = jaxbDetailVo.getValueAsString();
        if (value != null && !"".equals(value))
        {
          detailVo.setValueAsString(value);
          if (value.startsWith("#{"))
          {
            detailVo.setElExpressionUsed(true);
          }
        }
        // recursive call to add  detail param values
        addNestedValueObjects(detailVo, jaxbDetailVo);
      }
    }
  }

  private TaskFlowTesterType createJaxbModelFromXml(String xml)
  {
    TaskFlowTesterType tftester = null;
    String instancePath = TaskFlowTesterType.class.getPackage().getName();
    Unmarshaller u;
    try
    {
      JAXBContext jc = JAXBContext.newInstance(instancePath);
      u.set
      sLog.fine("JAXBContext: " + jc.getClass());
      u = jc.createUnmarshaller();
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      InputStream stream = getClass().getClassLoader().getResourceAsStream("org/emg/adf/tftester/rt/model/xml/tftester.xsd");
      Source source = new StreamSource(stream);
      Schema schema = factory.newSchema(source);
      u.setSchema(schema);
      tftester = (TaskFlowTesterType) u.unmarshal(new ByteArrayInputStream(xml.getBytes()));
    }
    catch (Exception exc)
    {
      throw new JboException("Error importing testcases from XML: " + exc.getMessage());
    }
    return tftester;
  }

  private TaskFlowTesterType createJaxbModelFromBeanModel()
  {
    ObjectFactory factory = new ObjectFactory();
    TaskFlowTesterType root = factory.createTaskFlowTester();
    for (TaskFlow tf: getTestTaskFlows())
    {
      TaskFlowType jaxbTf = factory.createTaskFlow();
      jaxbTf.setDisplayName(tf.getDisplayName());
      jaxbTf.setTaskFlowId(tf.getTaskFlowIdString());
      root.getTaskFlow().add(jaxbTf);
      // loop over TF testcases
      for (TaskFlowTestCase tc: tf.getTestCases())
      {
        TestCaseType jaxbTc = factory.createTestCase();
        jaxbTc.setDescription(tc.getDescription());
        jaxbTc.setName(tc.getName());
        jaxbTc.setRunAscall(tc.isRunAscall());
        jaxbTc.setRunInRegion(tc.isRunInRegion());
        jaxbTc.setStretchLayout(tc.isStretchLayout());
        jaxbTf.getTestCase().add(jaxbTc);
        // loop over param values
        if (tc.getParamValueObjects() != null)
        {
          for (ValueObject vo: tc.getParamValueObjects())
          {
            ParamValueObjectType jaxbVo = factory.createParamValueObject();
            jaxbVo.setName(vo.getName());
            jaxbVo.setClassName(vo.getClassName());
            jaxbVo.setValueAsString(vo.getValueAsString());
            // add  detail param values
            addNestedParamValues(factory, vo, jaxbVo);
            // add vo to testcase
            jaxbTc.getParamValueObject().add(jaxbVo);
          }
        }
      }
    }
    return root;
  }

  private void addNestedParamValues(ObjectFactory factory, ValueObject vo, ParamValueObjectType jaxbVo)
  {
    if (vo.getValueProperties() != null)
    {
      for (ValueObject detailVo: vo.getValueProperties())
      {
        ParamValueObjectType jaxbDetailVo = factory.createParamValueObject();
        jaxbDetailVo.setName(detailVo.getName());
        jaxbDetailVo.setClassName(detailVo.getClassName());
        jaxbDetailVo.setValueAsString(detailVo.getValueAsString());
        // recursive call to add  detail param values
        addNestedParamValues(factory, detailVo, jaxbDetailVo);
        // add vo to parent
        jaxbVo.getParamValueObject().add(jaxbDetailVo);
      }
    }
  }

  private String writeToXML(TaskFlowTesterType taskFlows)
  {
    String output = null;
    String instancePath = TaskFlowTesterType.class.getPackage().getName();
    try
    {
      JAXBContext jc = JAXBContext.newInstance(instancePath);
      sLog.fine("JAXBContext: " + jc.getClass());
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      sLog.fine("Marshaller: " + m.getClass());
      String encoding = "UTF-8";
      m.setProperty(Marshaller.JAXB_ENCODING, encoding);

      // format
      //      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      //      dbf.setNamespaceAware(false);
      //      DocumentBuilder db = dbf.newDocumentBuilder();
      //        XMLDocument doc = (XMLDocument) db.newDocument();
      //        m.marshal(service, doc); // new FileOutputStream("c:/temp/appstruct-test.xml"));
      //
      //        XmlFormatter xf = new XmlFormatter();
      //        output = xf.format(doc,"1.0",encoding);
      //      }
      //      else
      //      {
      StringWriter sw = new StringWriter();
      m.marshal(taskFlows, sw); // new FileOutputStream("c:/temp/appstruct-test.xml"));
      output = sw.toString();
      //      }

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return output;
  }

  private void loadDefaultTestcases()
  {
    InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("adf-emg-task-flow-tester.xml");
    if (stream!=null)
    {
      String xml = convertStreamToString(stream, "UTF-8");
      importFromXml(xml);
    }
  }

  /**
   * Nice trick from StackOverflow website to convert stream to string using standard library.
   * The reason it works is because Scanner iterates over tokens in the stream, and in this case we 
   * separate tokens using "beginning of the input boundary" (\A) thus giving us only one token 
   * for the entire contents of the stream.
   * @param is
   * @param encoding
   * @return
   */
  private String convertStreamToString(java.io.InputStream is, String encoding)
  {
    try
    {
      return new java.util.Scanner(is, encoding).useDelimiter("\\A").next();
    }
    catch (java.util.NoSuchElementException e)
    {
      return "";
    }
  }

  public static void main(String[] args)
  {
    TaskFlowTesterService s = new TaskFlowTesterService();
    s.loadDefaultTestcases();
    System.err.println(s.exportToXML());
  }

}
