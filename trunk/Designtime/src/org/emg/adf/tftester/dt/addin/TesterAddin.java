package org.emg.adf.tftester.dt.addin;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Map;

import javax.swing.JOptionPane;

import oracle.ide.Addin;
import oracle.ide.Ide;
import oracle.ide.model.Project;

import oracle.jdeveloper.library.JLibraryManager;
import oracle.jdeveloper.library.ProjectLibraryChangeEvent;
import oracle.jdeveloper.library.ProjectLibraryChangeListener;
import oracle.jdeveloper.runner.RunConfiguration;
import oracle.jdeveloper.runner.RunConfigurations;


public class TesterAddin
 // extends Command
  implements Addin
{

//  private static final Integer cmdId = Ide.findCmdID("org.emg.adf.tfTester");

//  public RunTester()
//  {
//    super(cmdId, NO_CHANGE);
//  }

//  @Override
// check source AdfcEmbeddedServerCallback
//  public int doit()
//  {
//
//    String[] libraries = new String[]
//      { "ADF EMG Task Flow Tester" };
//    JProjectUtil.addLibraries(Ide.getActiveProject(), libraries);
//
//    AdfcConfigNode cnode = (AdfcConfigNode) getContext().getNode();
//    XmlContext xmlContext = cnode.getXmlContext(context);
//    AbstractModel xmlModel = xmlContext.getActiveAbstractModel();
//    TaskFlow tf = AdfcSingleViewUtils.getSingleTaskFlow(xmlModel);
//    TaskFlowId tfi = tf.getTaskFlowId();
//    //    System.err.println("doc name: "+tfi.getDocumentName());
//    //    System.err.println("fully qual name: "+tfi.getFullyQualifiedName());
//    //    System.err.println("local tfi: "+tfi.getLocalTaskFlowId());
//    //    RunConfigurations.setActiveRunConfiguration(Ide.getActiveProject(), rc);
//    return 0;
//  }

  private static final String RUN_CONFIG_NAME = "Task Flow Tester";

  private void addTaskFlowTesterRunConfiguration()
  {
    // we first need to check whether TF Tester library is attached. This check is needed because when the
    // extension is not loaded, and the first action the user performs is DELETING the TF tester project from  
//    System.err.println("CHECK RUN CONFIG EXISTS .....");
    Project prj =  Ide.getActiveProject(); 
    if (!prj.isOpen()) {
            try {
//                System.err.println("OPEN PROJECT NOW");
                prj.open();
//                System.err.println("PROJECT OPENED");                
            } catch (IOException e) {
//                System.err.println("ERROR PROJECT OPENED: "+e.getMessage());                
                
            }
        }
    else {
 //       System.err.println(" PROJECT ALREADY OPEN");        
    }
    boolean exists = RunConfigurations.getRunConfigurationByName(Ide.getActiveProject(), RUN_CONFIG_NAME) !=null;
    if (exists)
    {
//        System.err.println("RUN CONFIG ALREADSY EXISTS .....");
      return;
    }
//    System.err.println("ADING RUN CONFIG.....");
    RunConfiguration rc = new RunConfiguration();
    rc.setCustom(false);
    rc.setName(RUN_CONFIG_NAME);
    String homeDir = Ide.getProductHomeDirectory();
    homeDir = homeDir.replace('\\', '/');
    String filePrefix = homeDir.startsWith("/") ? "file:" : "file:/";
    // Commented out: run tester inside its own task flow
    //      String jarloc =
    //        filePrefix + homeDir + "extensions/org.emg.adf.taskflowtester/org.emg.adf.AdfTaskFlowTesterRT.jar!/WEB-INF/adfemg/tftester/tester-tf.xml";
          String jarloc =
            filePrefix + homeDir + "extensions/org.emg.adf.taskflowtester/org.emg.adf.AdfTaskFlowTesterRT.jar!/adfemg/tftester/pages/tester.jsf";
    try
    {
      URL runUrl = new URL("jar", null, jarloc);
//        System.err.println("ADING RUN URL: "+runUrl);
      rc.setTargetURL(runUrl);
      rc.setAllowInput(true);
      RunConfigurations.addRunConfiguration(prj, rc);   
      RunConfigurations.setActiveRunConfiguration(prj, rc);
      String message =   "A run configuration named 'Task Flow Tester' has been added to the project as active run configuration."+ "\n" +
      "Use this run configuration to launch the ADF EMG Task Flow Tester."
                           ;
      JOptionPane.showMessageDialog(Ide.getMainWindow(), message
                                    ,"ADF EMG Task Flow Tester"
                                    ,JOptionPane.INFORMATION_MESSAGE);
    }
    catch (MalformedURLException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void initialize()
  {
//    System.err.println("INIT4 TFTESTER ADDIN");
    // add run config for current ptoject and register listener so it will be added for other projects when
    // user adds the ADF tftester lib
    registerLibraryChangeListener();
//    JProjectLibraryList.getInstance(Ide.getActiveProject().getProperties()).findLibrary(arg0)
    addTaskFlowTesterRunConfiguration();
  }

  private void registerLibraryChangeListener()
  {
//    System.err.println("REG LIB CHANGE");  
    ProjectLibraryChangeListener _listener = new ProjectLibraryChangeListener()
    {
      private static final String LIB_NAME = "ADF EMG Task Flow Tester";

      public void projectOpened(Project project)
      {
      }

      public void projectClosed(Project project)
      {
      }

      @Override
      public boolean needClasspathNotification(Project project)
      {
        return true;
      }

      @Override
      public void projectClasspathChanged(ProjectLibraryChangeEvent projectLibraryChangeEvent)
      {
//          System.err.println("CLASSPATH CHANGED");
        Map<String, Integer> addedLibs = projectLibraryChangeEvent.getLibsAddedToClasspath();
        boolean added = addedLibs.containsKey(LIB_NAME);
        if (added)
        {
//            System.err.println("CLASSPATH CHANGED - ENG ADDED!!! CALL ADDTFTRC");
          addTaskFlowTesterRunConfiguration();
        }
      }
    };
//    System.err.println("REGISTERING LIB CHANGE LISTENER.....");
    JLibraryManager instance = JLibraryManager.getInstance();
    instance.addLibraryChangeListener(_listener);
//      System.err.println("REGISTERING LIB CHANGE LISTENER..... DONE!");
  }

}
