 /*******************************************************************************
  Copyright: see readme.txt

  $revision_history$
  25-apr-2013   Steven Davelaar
  1.1           - Set run active file to false on tester run config
                - Set adfc run target to tftester view activity
                - Update ru target URL to point to correct hjdev home if needed
  07-jun-2012   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package org.emg.adf.tftester.dt.addin;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Map;

import javax.swing.JOptionPane;

import oracle.adfdt.view.rich.ADFFacesDTUtils;

import oracle.ide.Addin;
import oracle.ide.Ide;
import oracle.ide.log.LogManager;
import oracle.ide.model.Project;

import oracle.javatools.data.HashStructure;

import oracle.jdeveloper.library.JLibraryManager;
import oracle.jdeveloper.library.ProjectLibraryChangeEvent;
import oracle.jdeveloper.library.ProjectLibraryChangeListener;
import oracle.jdeveloper.runner.RunConfiguration;
import oracle.jdeveloper.runner.RunConfigurations;


/**
 * Add-in class that adds library listener. The listener checks whether the ADF EMG Task Flow Tester library is
 * added to the project, and if so, it adds a project run configuration to launch the tester task flow
 *
 * @author Steven Davelaar
 */
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


  /**
   * When project is versioned and multiple developers use different install dirs for JDev, the run target URL
   * might be invalid. Also when developers start using a new JDev version the run target should point to the 
   * JDev extension dir.
   * This method will update the run target URL if needed.
   * @return
   */
  private String getRunConfigTargetUrlPath()
  {
    String homeDir = Ide.getProductHomeDirectory();
    homeDir = homeDir.replace('\\', '/');
    String filePrefix = homeDir.startsWith("/") ? "file:" : "file:/";
    String jarloc =
      filePrefix + homeDir + "extensions/org.emg.adf.taskflowtester/org.emg.adf.AdfTaskFlowTesterRT.jar!/META-INF/adfc-config.xml";
    return jarloc;
  }
  
  private void updateRunConfigTargetUrlPathIfNeeded(Project project)
  {
    RunConfiguration rc = RunConfigurations.getRunConfigurationByName(project, RUN_CONFIG_NAME);
    if (rc!=null)
    {
      updateWebXmlIfNeeded();
      if (rc.isRunActiveFile())
      {
        rc.setRunActiveFile(false);              
      }
      String oldPath = rc.getTargetURL().getPath();
      String newPath = getRunConfigTargetUrlPath(); 
      if (!newPath.equals(oldPath))
      {
        try
        {
          URL runUrl = new URL("jar", null, newPath);
          rc.setTargetURL(runUrl);          
//  Unfortunately, showing this dialog does not work: JDev hangs and the dialog only contains the tite and not the message.
           String message =   "ADF EMG Task Flow Tester INFO: The default run target of the 'Task Flow Tester' run configuration has been updated \nto point to the correct location of the ADF EMG Task Flow Tester Runtime jar file."
                                ;
          LogManager.getIdeLogWindow().log(message);
// Showing a dialog makes JDev hang in 11.1.1.x, so for consistency we also use logger line in 11.1.2
//           JOptionPane.showMessageDialog(Ide.getMainWindow(), message
//                                         ,"ADF EMG Task Flow Tester"
//                                         ,JOptionPane.INFORMATION_MESSAGE);
        }
        catch (MalformedURLException e)
        {
          e.printStackTrace();
        }
      }  
    }
//    else
//    {
//      System.err.println("****** NOOOOOOOO TF TESTER RUN CONFIG IN PRJ "+project.getShortLabel());
//    }
  }

  private void addTaskFlowTesterRunConfiguration()
  {
 //    System.err.println("CHECK RUN CONFIG EXISTS .....");


    RunConfiguration existing = RunConfigurations.getRunConfigurationByName(Ide.getActiveProject(), RUN_CONFIG_NAME);
    boolean exists = existing !=null;
    if (exists)
    {
      return;
    }
 //    System.err.println("ADING RUN CONFIG.....");
    RunConfiguration rc = new RunConfiguration();
    rc.setCustom(false);
    rc.setName(RUN_CONFIG_NAME);
 // Before JDev 11.1.1.6 we cann run the tester within unbounde dtask flow using tester page
 // We must run it inside its own task flow and mempory scope by running the tf-tester task flow
 //   float version = Ide.getVersion();
 //      String runFile = version >= 11.116 ? "/adfemg/tftester/pages/tester.jspx" : "/WEB-INF/adfemg/tftester/tester-tf.xml";
 //      String runFile = version >= 11.116 ? "/adfemg/tftester/pages/tester.jspx" : "/META-INF/adfc-config.xml";
    try
    {
      URL runUrl = new URL("jar", null, getRunConfigTargetUrlPath());
      rc.setTargetURL(runUrl);
      rc.setAllowInput(true);
      rc.setRunActiveFile(false);
      
      HashStructure adfcRunSettings = rc.getProperties().getOrCreateHashStructure("oracle.adfdt.controller.adfc.source.runner.AdfcRunnerLaunchSettings");
      adfcRunSettings.putString("viewActivityId", "tftester");

      RunConfigurations.addRunConfiguration(Ide.getActiveProject(), rc);    
      String message =   "A run configuration named 'Task Flow Tester' has been added to the project run configurations."+ "\n" +
      "Use this run configuration to launch the ADF EMG Task Flow Tester."
                           ;
      updateWebXmlIfNeeded();      

      JOptionPane.showMessageDialog(Ide.getMainWindow(), message
                                    ,"ADF EMG Task Flow Tester"
                                    ,JOptionPane.INFORMATION_MESSAGE);
    }
    catch (MalformedURLException e)
    {
      e.printStackTrace();
    }
  }
  
  private void updateWebXmlIfNeeded()
  {
    // register javax.faces.FACELETS_VIEW_MAPPINGS context param, in case project only contains fragments, not pages
    ADFFacesDTUtils.configureFaceletsSuffix(Ide.getActiveProject(), ".jsf");      
    ADFFacesDTUtils.enableJarResourceSupport(Ide.getActiveProject());    
  }

  @Override
  public void initialize()
  {
 //    System.err.println("INIT4 TFTESTER ADDIN");
    // add run config for current ptoject and register listener so it will be added for other projects when
    // user adds the ADF tftester lib
 //    addTaskFlowTesterRunConfiguration();
    registerLibraryChangeListener();
  }

  private void registerLibraryChangeListener()
  {
    ProjectLibraryChangeListener _listener = new ProjectLibraryChangeListener()
    {
      private static final String LIB_NAME = "ADF EMG Task Flow Tester";

      public void projectOpened(Project project)
      {
        updateRunConfigTargetUrlPathIfNeeded(project);
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
        Map<String, Integer> addedLibs = projectLibraryChangeEvent.getLibsAddedToClasspath();
        boolean added = addedLibs.containsKey(LIB_NAME);
        if (added)
        {
          addTaskFlowTesterRunConfiguration();
        }
      }
    };
 //    System.err.println("REGISTERING LIB CHANGE LISTENER.....");
    JLibraryManager instance = JLibraryManager.getInstance();
    instance.addLibraryChangeListener(_listener);
  }

 }
