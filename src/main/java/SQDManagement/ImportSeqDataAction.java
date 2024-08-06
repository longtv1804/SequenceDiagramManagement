package SQDManagement;

import java.awt.FileDialog;
import java.awt.Frame;

import javax.swing.JOptionPane;

import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;

public class ImportSeqDataAction implements IPluginActionDelegate {

	public Object run(IWindow window) throws UnExpectedException {
	    AstahGateway gw = new AstahGateway(window);
	    if (gw.isProjectExisted() == false) {
			JOptionPane.showMessageDialog(window.getParent(), "You must open a project first.");
	    	return null;
	    }
	    // select file
	    FileDialog fd = new FileDialog(new Frame("select file"), "Choose Imported input file", FileDialog.LOAD);
	    fd.setFile("*.xlsx");
	    fd.setVisible(true);
	    
	    if (Utils.isEmpty(fd.getDirectory()) == false && Utils.isEmpty(fd.getFile()) == false) {
		    String filePath = fd.getDirectory() + fd.getFile();
		    
		    // running import file's data to astah project
		    Log.setPath(fd.getDirectory() + "/Import.log");
		    ImportSeqDataController controller = new ImportSeqDataController(gw, filePath);
		    try {
		    	controller.run();
		    } catch (Exception e) {
				gw.showMessage("Exception when import: " + e.getMessage());
				Log.e("ImportSeqDataAction", "Exeption: " + e.getMessage());
				StackTraceElement[] traces = e.getStackTrace();
				for (StackTraceElement element : traces) {
					Log.e("ImportSeqDataAction", element.getClassName() + ":" + element.getLineNumber() + " " + element.getMethodName());
				}
			}
	    }
		return null;
	}
}