package org.emg.adf.tftester.rt.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.JboException;

import org.emg.adf.tftester.rt.model.xml.jaxb.ObjectFactory;
import org.emg.adf.tftester.rt.model.xml.jaxb.ParamValueObjectType;
import org.emg.adf.tftester.rt.model.xml.jaxb.TaskFlowTesterType;
import org.emg.adf.tftester.rt.model.xml.jaxb.TaskFlowType;
import org.emg.adf.tftester.rt.model.xml.jaxb.TestCaseType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class TaskFlowTesterService
{
  private List<TaskFlow> testTaskFlows = new ArrayList<TaskFlow>();
  private Map<String, TaskFlow> testTaskFlowsMap = new HashMap<String,TaskFlow>();
  ADFLogger sLog = ADFLogger.createADFLogger(TaskFlowTesterService.class);

  public TaskFlowTesterService()
  {
    super();
    loadDefaultTestcases();
  }

  public TaskFlow addTaskFlow(String taskFlowId, String displayName, boolean throwError, boolean returnExisting)
  {
    boolean exists = testTaskFlowsMap.containsKey(taskFlowId);
    if (exists)
    {
      if (returnExisting)
      {
        return testTaskFlowsMap.get(taskFlowId);        
      }
      else if (throwError)
      {
        throw new JboException("Task flow with this id already exists.");
      }
      else
      {
        return null;
      }
    }
    TaskFlow tf = new TaskFlow();
    tf.setTaskFlowIdString(taskFlowId);
    try
    {
      tf.getTaskFlowDefinition();
      tf.setDisplayName(displayName);    
      // call getDisplayName as final test, will throw NPE when task flow path is OK but id after # is wrong
      tf.getDisplayName();
      getTestTaskFlows().add(tf);       
      testTaskFlowsMap.put(taskFlowId, tf);
    }
    catch (Exception e)
    {
      if (throwError)
      {
        throw new JboException("Invalid task flow id");        
      }
    }
    return tf;
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
    try
    {
      TaskFlowTesterType tftester = createJaxbModelFromXml(xml);
      createBeanModelFromJaxbModel(tftester);
    }
    catch (Exception exc)
    {
      throw new JboException("Error importing testcases from XML: " +
                             exc.getMessage());
    }
    // create bean model from jaxbmodel
  }

  private void createBeanModelFromJaxbModel(TaskFlowTesterType tftester)
  {
    for (Object jaxbTfObject: tftester.getTaskFlow())
    {
      TaskFlowType jaxbTf = (TaskFlowType) jaxbTfObject;
      //      TaskFlow tf = new TaskFlow();
      //      tf.setDisplayName(jaxbTf.getDisplayName());
      //      tf.setTaskFlowIdString(jaxbTf.getTaskFlowId());
      //      getTestTaskFlows().add(tf);
      // last argument is true so addTaskFlow will return existing task flow if present, so new testcases
      // will be added under existing node    
      TaskFlow tf = addTaskFlow(jaxbTf.getTaskFlowId(), jaxbTf.getDisplayName(), false, true);  
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
            ParamValueObjectType jaxbVo =
              (ParamValueObjectType) jaxbVoObject;
            ValueObject vo =
              new ValueObject(jaxbVo.getName(), jaxbVo.getClassName(),
                              null, false);
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

  private void addNestedValueObjects(ValueObject vo,
                                     ParamValueObjectType jaxbVo)
  {
    if (jaxbVo.getParamValueObject() != null)
    {
      for (Object jaxbDetailVoObject: jaxbVo.getParamValueObject())
      {
        ParamValueObjectType jaxbDetailVo =
          (ParamValueObjectType) jaxbDetailVoObject;
        ValueObject detailVo =
          new ValueObject(jaxbDetailVo.getName(), jaxbDetailVo.getClassName(),
                          vo, vo.isMapType());
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
    throws JAXBException
  {
    String instancePath = TaskFlowTesterType.class.getPackage().getName();
    Unmarshaller u;
    JAXBContext jc = JAXBContext.newInstance(instancePath);
    sLog.fine("JAXBContext: " + jc.getClass());
    u = jc.createUnmarshaller();
    //      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    //      InputStream stream = getClass().getClassLoader().getResourceAsStream("org/emg/adf/tftester/rt/model/xml/tftester.xsd");
    //      Source source = new StreamSource(stream);
    //      Schema schema = factory.newSchema(source);
    //      u.setSchema(schema);
    TaskFlowTesterType tftester =
      (TaskFlowTesterType) u.unmarshal(new ByteArrayInputStream(xml.getBytes()));
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

  private void addNestedParamValues(ObjectFactory factory, ValueObject vo,
                                    ParamValueObjectType jaxbVo)
  {
    if (vo.getValueProperties() != null)
    {
      for (ValueObject detailVo: vo.getValueProperties())
      {
        ParamValueObjectType jaxbDetailVo =
          factory.createParamValueObject();
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
      m.marshal(taskFlows,
                sw); // new FileOutputStream("c:/temp/appstruct-test.xml"));
      output = sw.toString();
      //      }

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return output;
  }

  /**
   * Load all task flows and testcases that are defined in adf-emg-task-flow-tester.xml on the classpath.
   * If multiple files are found, they will all be loaded, unless the task flow or testcase is already in the list.
   */
  private void loadDefaultTestcases()
  {
    try
    {
      Enumeration<URL> enump =
        Thread.currentThread().getContextClassLoader().getResources("adf-emg-task-flow-tester.xml");
      while (enump.hasMoreElements())
      {
        try
        {
          URL url = enump.nextElement();
          InputStream stream = url.openStream();
          String xml = convertStreamToString(stream, "UTF-8");
          importFromXml(xml);
        }
        catch (IOException e)
        {          
          // we catch IO exception here as well, so we go to next resource
        }
      }
    }
    catch (IOException e)
    {
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
  private String convertStreamToString(java.io.InputStream is,
                                       String encoding)
  {
    try
    {
      return new java.util.Scanner(is,
                                   encoding).useDelimiter("\\A").next();
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

  public void loadAvailableTaskFlows(String startDir, boolean recurseInSubDirs,
                                boolean searchAdfLibs)
  {
    List<String> taskFlows = new ArrayList<String>();
    addLocalTaskFlows(taskFlows, startDir, recurseInSubDirs);
    if (searchAdfLibs)
    {
      addAdfLibraryTaskFlows(taskFlows);
    }
    // loop over discovered task flow id's, and call addTaskFlow
    // In addTaskFlow we check whether the atask flow is already added
    for (String taskFlowId : taskFlows)
    {
      addTaskFlow(taskFlowId, null, false, false);
    }
  }

  private void addAdfLibraryTaskFlows(List<String> taskflows)
  {
    try
    {
      DocumentBuilderFactory dbFactory =
        DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Enumeration<URL> enump =
        Thread.currentThread().getContextClassLoader().getResources("META-INF/task-flow-registry.xml");
      while (enump.hasMoreElements())
      {
        try
        {
          URL url = enump.nextElement();
          InputStream is = url.openStream();
          Document doc = dBuilder.parse(is);
          NodeList tfList =
            doc.getDocumentElement().getElementsByTagName("task-flow-descriptor");
          for (int i = 0; i < tfList.getLength(); i++)
          {
            Element tf = (Element) tfList.item(i);
            String path = tf.getAttribute("path");
            String id = tf.getAttribute("id");
            String tfId = path + "#" + id;
            String type = tf.getAttribute("type");
            String internal = tf.getAttribute("library-internal");
            if ("task-flow-definition".equals(type) &&
                "false".equals(internal))
            {
              taskflows.add(tfId);
            }
          }
        }
        catch (SAXException e)
        {
          // ignore errors, continue with next tf registry file
        }
      }
    }
    catch (IOException e)
    {
      //
    }
    catch (ParserConfigurationException e)
    {
    }
  }

  public void addLocalTaskFlows(List<String> taskFlows, String startDir,
                                boolean recursive)
  {
    Set<String> set =
      FacesContext.getCurrentInstance().getExternalContext().getResourcePaths(startDir);
    if (set==null)
    {
      return;
    }
    for (String resourcePath: set)
    {
      if (resourcePath.endsWith(".xml"))
      {
        addTaskFlowIdFromResourcePath(taskFlows, resourcePath);
      }
      else
      {
        try
        {
          URL url =
            FacesContext.getCurrentInstance().getExternalContext().getResource(resourcePath);
          File file = new File(url.toURI());
          if (file.isDirectory() && recursive)
          {
            addLocalTaskFlows(taskFlows, resourcePath, recursive);
          }
        }
        catch (MalformedURLException e)
        {
        }
        catch (URISyntaxException e)
        {
        }
      }
    }
  }

  private void addTaskFlowIdFromResourcePath(List<String> taskFlows,
                                             String resourcePath)
  {
    TaskFlowXMLHandler handler = null;
    try
    {
      InputStream is =
        FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(resourcePath);
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      handler = new TaskFlowXMLHandler();
      saxParser.parse(is, handler);
    }
    catch (ParserConfigurationException e)
    {
    }
    catch (SAXException e)
    {
    }
    catch (IOException e)
    {
    }
    if (handler != null && handler.getTaskFlowId() != null)
    {
      String path = resourcePath+"#"+handler.getTaskFlowId();
      taskFlows.add(path);
    }
  }

  /**
   * private class to parse an XML document and check whether it is a bounded task flow definition.
   * We use the Sax parser to increase performance and reduce memory usage because
   * we might very large xml files.
   */
  class TaskFlowXMLHandler
    extends DefaultHandler
  {
    int elcount = 0;
    String taskFlowId;

    public void startDocument()
    {
      elcount = 0;
      taskFlowId = null;
    }

    public String getTaskFlowId()
    {
      return taskFlowId;
    }

    public void startElement(String uri, String localName, String qName,
                             Attributes attributes)
      throws SAXException
    {
      elcount++;
      if (elcount == 2 && qName.equalsIgnoreCase("task-flow-definition"))
      {
        taskFlowId = attributes.getValue("", "id");
      }
      // task-flow-definition is second element, so always stop prcoessing now
      if (elcount == 2)
      {
        throw new SAXException("done");
      }
    }

    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {

    }

    public void characters(char[] ch, int start, int length)
      throws SAXException
    {
    }
  }
}
