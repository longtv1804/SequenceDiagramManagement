package SQDManagement;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;

public class ClearDataAction implements IPluginActionDelegate {

	public Object run(IWindow window) throws UnExpectedException {
		try {
		    // create gateway, controller -> run export
		    AstahGateway gw = new AstahGateway(window);
		    if (gw.isProjectExisted() == false) {
				JOptionPane.showMessageDialog(window.getParent(), "You must open a project first.");
		    	return null;
		    }
		    
		    ClearDataController controller = new ClearDataController(gw);
		    controller.run();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(window.getParent(), e.getMessage());
		}
	    return null;
	}
	
}