package SQDManagement;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.*;

import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.IPresentation;

public class ExportSeqDataController {
	private static String TAG = "ExportSeqDataController";
	protected AstahGateway mGateway = null;
	protected String mDirPath = null;
	protected int mTagId = 0;
	
	private XlsxWriter mWriter = null;
	
	ExportSeqDataController (AstahGateway gw, String dirPath) {
		mGateway = gw;
		mDirPath = dirPath;		
	}
	
	

	// ===========================================================================================
	//										export functions
	// ===========================================================================================
	
	private static boolean isKeyValid (String key) {
		if (Utils.isEmpty(key)) {
			return false;
		}
		if (key.startsWith("#") == false) {
			return false;
		}
		return true;
	}
	protected void export (ISequenceDiagram sq) throws Exception {
		if (sq == null) return;
		Log.d(TAG, "start export ISequenceDiagram: " + sq.getName());
		
		mWriter = new XlsxWriter(mDirPath, sq.getName());
		mWriter.writeData("#sequence diagram name", sq.getName());
		
		Set<String> keySet = new HashSet<String>();
		
		// export LifeLine
		mWriter.setNextColorForHeader();
		ArrayList<IPresentation> lifeLines = mGateway.getLifeLines(sq);
		for (IPresentation ipst : lifeLines) {
			ILifeline item = (ILifeline)(ipst.getModel());
			
			// write to .xlsx
			String itemName = Utils.trim(item.getName());
			if (isKeyValid(itemName) == false || keySet.contains(itemName)) {
				Log.e(TAG, "export() ILifeline: the item '" + itemName + "' is invalid or existed multiple times");
				continue;
			}
			Log.d(TAG, "export() ILifeline: " + itemName);
			mWriter.writeData(itemName, "");
			mGateway.addDDTag(item, itemName);
			keySet.add(itemName);
		}
		

		// export IMessages
		mWriter.setNextColorForHeader();
		ArrayList<IPresentation> messages = mGateway.getMessage(sq);
		for (IPresentation ipst : messages) {
			IMessage item = (IMessage)(ipst.getModel());

			// write to .xlsx
			String itemName = Utils.trim(item.getName());
			if (isKeyValid(itemName) == false || keySet.contains(itemName)) {
				Log.e(TAG, "export() IMessage: the KEY item '" + itemName + "' is invalid or existed multiple times");
				continue;
			}
			Log.d(TAG, "export() IMessage: " + itemName + "(param)");
			mWriter.writeData(itemName, "fucntionName(param1, param2, param3)");
			mGateway.addDDTag(item, itemName);
			keySet.add(itemName);
		}
		
		// export CombineFragments
		mWriter.setNextColorForHeader();
		ArrayList<IPresentation> combinedFragments = mGateway.getCombineFragments(sq);
		for (IPresentation ipst : combinedFragments) {
			ICombinedFragment item = (ICombinedFragment)(ipst.getModel());

			// write to .xlsx
			String itemName = Utils.trim(item.getName());
			if (isKeyValid(itemName) == false || keySet.contains(itemName)) {
				Log.e(TAG, "export() ICombinedfragment: the KEY item '" + itemName + "' is invalid or existed multiple times");
				continue;
			}
			String data = "name:" + itemName + "\n";
			data += "guards:";
			IInteractionOperand iItOp[] = item.getInteractionOperands();
			for (IInteractionOperand ito : iItOp) {
				data += "\n@guard example";
			}
			Log.d(TAG, "export() ICombinedFragment: " + itemName);
			mWriter.writeData(itemName, data);
			mGateway.addDDTag(item, itemName);
			keySet.add(itemName);
		}
		
		// export comments
		ArrayList<IPresentation> comments = mGateway.getComments(sq);
		for (IPresentation ipst : comments) {
			IComment item = (IComment)(ipst.getModel());
			
			// write to .xlsx
			String itemName = item.getBody();
			if (isKeyValid(itemName) == false || keySet.contains(itemName)) {
				Log.e(TAG, "export() IComment: the KEY item '" + itemName + "' is invalid or existed multiple times");
				continue;
			}
			Log.d(TAG, "export() IComment: " + itemName);
			mWriter.writeData(itemName, "content for comment here");
			mGateway.addDDTag(item, itemName);
			keySet.add(itemName);
		}
			
		// export InteractionUse
		ArrayList<IPresentation> iIttUs = mGateway.getInteractionUse(sq);
		for (IPresentation ipst : iIttUs) {
			IInteractionUse item = (IInteractionUse)(ipst.getModel());
			
			// write to .xlsx
			String itemName = item.getName();
			if (isKeyValid(itemName) == false || keySet.contains(itemName)) {
				Log.e(TAG, "export() InteractionUse: the KEY item '" + itemName + "' is invalid or existed multiple times");
				continue;
			}
			String data = "name:" + itemName + "\n";
			data += "@name of sequence diagaram here";
			Log.d(TAG, "export() InteractionUse: " + itemName);
			mWriter.writeData(itemName, data);
			mGateway.addDDTag(item, itemName);
			keySet.add(itemName);
		}
		
		mWriter.save();
		Log.d(TAG, "export() sequenceDiagram: DONE. " + sq.getName());
	}

	// ===========================================================================================
	//										run function
	// ===========================================================================================
	public void run () throws Exception {
		Log.d(TAG, "+++++++++++ Start Export ++++++++++++++");
		
		// only export sequence diagram
		IDiagram diagram = mGateway.getOpenedDiagram();
		if (diagram == null) {
			mGateway.showMessage("can't see any sequence is opened");
			return;
		}
		
		if (diagram instanceof ISequenceDiagram) {
			Log.d(TAG, "Diagram: " + diagram.getName());
			export((ISequenceDiagram)diagram);
		}
		
		mGateway.showMessage("Export Sequence.. Done.\n");
		Log.d(TAG, "export DONE\n");
	}
}