package SQDManagement;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;

public class ExportSeqDataAction implements IPluginActionDelegate {

	public Object run(IWindow window) throws UnExpectedException {
		try {
		    AstahGateway gw = new AstahGateway(window);
		    if (gw.isProjectExisted() == false) {
				JOptionPane.showMessageDialog(window.getParent(), "You must open a project first.");
		    	return null;
		    }
		    
		    // select directory for output
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			Integer res = jfc.showSaveDialog(null);
			
			if (res == JFileChooser.APPROVE_OPTION) {
			    File file = jfc.getSelectedFile();
			    
			    // create gateway, controller -> run export
			    Log.setPath(file.getAbsolutePath() + "/Export.log");
			    ExportSeqDataController controller = new ExportSeqDataController(gw, file.getAbsolutePath());
			    controller.run();
			} else if (res == JFileChooser.ERROR_OPTION) {
				JOptionPane.showMessageDialog(window.getParent(), "can not get output directory");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(window.getParent(), e.getMessage());
			Log.e("ExportSeqDataAction", "Exeption: " + e.getMessage());
			StackTraceElement[] traces = e.getStackTrace();
			for (StackTraceElement element : traces) {
				Log.e("ExportSeqDataAction", element.getClassName() + ":" + element.getLineNumber() + " " + element.getMethodName());
			}
		}
	    return null;
	}
}