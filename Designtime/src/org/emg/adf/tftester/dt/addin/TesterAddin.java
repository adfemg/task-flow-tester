/*******************************************************************************
Copyright (c) 2006 Oracle Corporation

Open Issues :

$revision_history$
 07-jun-2012  Steven Davelaar
   1.0        initial creation

******************************************************************************/
package org.emg.adf.tftester.dt.addin;

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

  private void addTaskFlowTesterRunConfiguration()
  {
//    System.err.println("CHECK RUN CONFIG EXISTS .....");
    boolean exists = RunConfigurations.getRunConfigurationByName(Ide.getActiveProject(), RUN_CONFIG_NAME) !=null;
    if (exists)
    {
      return;
    }
//    System.err.println("ADING RUN CONFIG.....");
    RunConfiguration rc = new RunConfiguration();
    rc.setCustom(false);
    rc.setName(RUN_CONFIG_NAME);
    String homeDir = Ide.getProductHomeDirectory();
    homeDir = homeDir.replace('\\', '/');
// Commented out: run tester inside its own task flow
//      String jarloc =
//        "file:/" + homeDir + "extensions/org.emg.adf.taskflowtester/org.emg.adf.AdfTaskFlowTesterRT.jar!/WEB-INF/adfemg/tftester/tester-tf.xml";
      String jarloc =
        "file:/" + homeDir + "extensions/org.emg.adf.taskflowtester/org.emg.adf.AdfTaskFlowTesterRT.jar!/adfemg/tftester/pages/tester.jspx";
    try
    {
      URL runUrl = new URL("jar", null, jarloc);
      rc.setTargetURL(runUrl);
      rc.setAllowInput(true);
      RunConfigurations.addRunConfiguration(Ide.getActiveProject(), rc);    
      String message =   "A run configuration named 'Task Flow Tester' has been added to the project run configurations."+ "\n" +
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
