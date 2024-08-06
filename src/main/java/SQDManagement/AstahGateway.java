package SQDManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.osgi.service.log.LogEntry;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.editor.SequenceDiagramEditor;
import com.change_vision.jude.api.inf.editor.IModelEditorFactory;
import com.change_vision.jude.api.inf.exception.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.project.ModelFinder;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.ui.IWindow;

import SQDManagement.Log;

class AstahGateway {
	private static final String TAG = "AstahGateway";

	private IWindow mWindow = null;
	private ProjectAccessor mProjectAccessor = null;
	
	public AstahGateway (IWindow win) {
		mWindow = win;
	    try {
	    	if (mProjectAccessor == null) {
		        AstahAPI api = AstahAPI.getAstahAPI();
		        mProjectAccessor = api.getProjectAccessor();
		        mProjectAccessor.getProject();      
	    	}
	    } catch (Exception e) {
	    	mProjectAccessor = null;
	    }
	}
	
	boolean isProjectExisted() {
		try {
			if (mProjectAccessor != null && mProjectAccessor.getProject() != null) {
				return true;
			}
		}catch (Exception e) {
			// ignore
		}
		return false;
	}
	
	@Override
	public void finalize() {
		mWindow = null;
	}
	
	public void showMessage (String msg) {
		if (mWindow != null) {
			JOptionPane.showMessageDialog(mWindow.getParent(), msg);
		}
	}
	
	public void save () throws Exception {
		try {
			mProjectAccessor.save();
		} catch (Exception e) {
			Log.e(TAG, "save() Exception: " + e.getMessage());
			throw new Exception("ERROR: can not save the projecte ??");
		}
	}
	
	
	/* =====================================================================================================
	 * 			get items
	 * =====================================================================================================
	 */
	public INamedElement getProject () {
		if (mProjectAccessor != null) {
			try {
				return mProjectAccessor.getProject();
			} catch (ProjectNotFoundException e) {
				// ignore
			}
		}
		return null;
	}
	
	public IDiagram getOpenedDiagram () {
		if (mProjectAccessor != null) {
			try {
				return mProjectAccessor.getViewManager().getDiagramViewManager().getCurrentDiagram();
			} catch (InvalidUsingException e) {
				Log.e(TAG, "getOpenedDiagram() Exception: " + e.getMessage());
			}
		}
		return null;
	}

	public IDiagram[] getOpenedDiagrams () {
		if (mProjectAccessor != null) {
			try {
				return mProjectAccessor.getViewManager().getDiagramViewManager().getOpenDiagrams();
			} catch (InvalidUsingException e) {
				Log.e(TAG, "getOpenedDiagrams() Exception: " + e.getMessage());
			}
		}
		return null;
	}
	
	public String getProjectName () {
		String res = null;
		if (mProjectAccessor != null) {
			try {
				res = mProjectAccessor.getProject().getName();
			} catch (ProjectNotFoundException e) {
				// ignore
			}
		}
		return res;
	}
	
	public INamedElement[] getPackages () {
		try {
			return mProjectAccessor.findElements(IPackage.class);
		} catch (Exception e) {
			Log.e(TAG, "getPackages() Exception: " + e.getMessage());
			return null;
		}
	}
	
	public INamedElement[] getClasses() {
		INamedElement[] res = null;
		if (mProjectAccessor != null) {
		    try {
				res = mProjectAccessor.findElements(IClass.class);
			} catch (ProjectNotFoundException e) {
				Log.e(TAG, "getClasses() Exception: " + e.getMessage());
			}
		}
		return res;
	}
	
	public INamedElement[] getSequenceDiagrams() {
		INamedElement[] dgms = null;
		if (mProjectAccessor != null) {
			try {
				dgms = mProjectAccessor.findElements(ISequenceDiagram.class);
			} catch (ProjectNotFoundException e) {
				Log.e(TAG, "getSequenceDiagrams() Exception: " + e.getMessage());
			}
		}
		return dgms;
	}
	
	public INamedElement[] getStateMachineDiagrams() {
		INamedElement[] dgms = null;
		if (mProjectAccessor != null) {
			try {
				dgms = mProjectAccessor.findElements(IStateMachineDiagram.class);
			} catch (ProjectNotFoundException e) {
				Log.e(TAG, "getStateMachineDiagrams() Exception: " + e.getMessage());
			}
		}
		return dgms;
	}
	
	public IPresentation[] getPresentation(IDiagram diagram) {
		if (diagram == null) {
			return null;
		}
	    try {
			return diagram.getPresentations();
		} catch (InvalidUsingException e) {
			Log.e(TAG, "getPresentation(IDiagram) Exception: " + e.getMessage());
			return null;
		}
	}
	
	/*
	 * =======================================================================================
	 * 					get items in a sequnece diagran
	 * =======================================================================================
	 */
	ArrayList<IPresentation> getTexts (IDiagram sq) {
		IPresentation[] psts = getPresentation(sq);
		if (psts == null) {
			return null;
		}
		ArrayList<IPresentation> res = new ArrayList<>();
		for (int i = 0; i < psts.length; i++) {			
			if ("Text".equals(psts[i].getType()) == true) {
				res.add(psts[i]);
			}
		}
		return res;
	}
	
	// func to get lifeLine in a sequence
	ArrayList<IPresentation> getLifeLines (IDiagram sq) {
		IPresentation[] psts = getPresentation(sq);
		if (psts == null) {
			return null;
		}
		ArrayList<IPresentation> res = new ArrayList<>();
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
			
			if (item instanceof ILifeline) {
				res.add(psts[i]);
			}
		}
		return res;
	}
	
	// func to get Message between lifeLines in a sequence
	ArrayList<IPresentation> getMessage (IDiagram sq) {
		IPresentation[] psts = getPresentation(sq);
		if (psts == null) {
			return null;
		}
		ArrayList<IPresentation> res = new ArrayList<>();
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
			
			if (item instanceof IMessage) {
				res.add(psts[i]);
			}
		}
		return res;
	}
	
	// func to get combineFragment in a sequence
	ArrayList<IPresentation> getCombineFragments (IDiagram sq) {
		IPresentation[] psts = getPresentation(sq);
		if (psts == null) {
			return null;
		}
		ArrayList<IPresentation> res = new ArrayList<>();
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
			
			if (item instanceof ICombinedFragment) {
				res.add(psts[i]);
			}
		}
		return res;
	}
	
	// func to get comments in a sequence
	ArrayList<IPresentation> getComments (IDiagram sq) {
		IPresentation[] psts = getPresentation(sq);
		if (psts == null) {
			return null;
		}
		ArrayList<IPresentation> res = new ArrayList<>();
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
			
			if (item instanceof IComment) {
				res.add(psts[i]);
			}
		}
		return res;
	}
	
	// func to get IInteractionUse in a sequence
	ArrayList<IPresentation> getInteractionUse (IDiagram sq) {
		IPresentation[] psts = getPresentation(sq);
		if (psts == null) {
			return null;
		}
		ArrayList<IPresentation> res = new ArrayList<>();
		for (int i = 0; i < psts.length; i++) {
			IElement item = psts[i].getModel();
			
			if (item instanceof IInteractionUse) {
				res.add(psts[i]);
			}
		}
		return res;
	}

	/* =====================================================================================================
	 * 													tag handle
	 * =====================================================================================================
	 */
	
	// check if the element has the tag likes "DDTAG"
	public boolean hasDDTag (IElement ele) {
		if (ele != null) {
			return getTag(ele) != null;
		}
		return false;
	}
	
	// find the tag as the tag likes DDTAG
	public String getDDTagValue (IElement ele) {
		if (ele != null && hasDDTag(ele)) {
			return ele.getTaggedValue(Constants.DDTAG);
		}
		return "";
	}
	
	// find and return the ITaggedValue, only return the first found
	public ITaggedValue getTag (IElement ele) {
		if (ele != null) {
			ITaggedValue[] tags = ele.getTaggedValues();
			if (tags != null) {
				for (int i = 0; i < tags.length; i++) {
					if (tags[i].getKey().equals(Constants.DDTAG) == true) {
						return tags[i];
					}
				}
			}
		}
		return null;
	}
	
	// set DDTAG to a element
	public boolean addDDTag (INamedElement ele, String tagValue) {
		if (mProjectAccessor == null || ele == null || tagValue == null) {
			return false;
		}

		ITaggedValue eleTag = getTag(ele);
		if (eleTag != null) {
			deleteTag(eleTag);
		}
		
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			BasicModelEditor editor = mProjectAccessor.getModelEditorFactory().getBasicModelEditor();
			editor.createTaggedValue(ele, Constants.DDTAG, tagValue);
			transaction.endTransaction();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "addTag() Exception: " + ele.getName() + " " + tagValue + " " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
		return false;
	}
	
	// remove the DDTAG
	public void deleteTag (ITaggedValue tag) {
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			BasicModelEditor editor = mProjectAccessor.getModelEditorFactory().getBasicModelEditor();
			editor.delete(tag);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "deleteTag(ITaggedValue) Exception:  " + e.getMessage() + " tag=" + tag);
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}
	
	// return all the INamedElement has DDTAG
	public INamedElement[] getElementsWithTag () {
		try {
			return mProjectAccessor.findElements(new ModelFinder() {
				@Override
				public boolean isTarget(INamedElement ele) {
					return hasDDTag(ele);
				}
			});
		} catch (ProjectNotFoundException e) {
			Log.e(TAG, "getElementsWithTag() Exception: " + e.getMessage());
			return null;
		}
	}
	
	
	/* ===============================================================================================
	 * 					import functions
	 * ===============================================================================================
	 */
	
	// with the INamedElement, just set the name
	public void importData (INamedElement element, String str) {
		if (element == null || str == null) {
			return;
		}
		
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			element.setName(str);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "importData(INamedElement) Exception: [" + element.getName() + "] - [" + str + "] exeption: " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}
	
	void setBaseForILifeline (ILifeline ill, IClass base) {
		if (ill == null) {
			return;
		}
		
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			ill.setBase(base);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "setBaseForILifeline() Exeption: " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}
	
	void importData(IMessage iMsg, IOperation refOp, String arg, String returnValue) {
		if (iMsg == null) {
			return;
		}
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			iMsg.setOperation(refOp);
			iMsg.setArgument(arg);
			iMsg.setReturnValue(returnValue);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "importData(IMessage, IOperation) Exception: [" + iMsg.getName() + "] " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}
	// use for IMessage
	void importData(IMessage iMsg, String name, String arg, String returnValue) {
		if (iMsg == null) {
			return;
		}
		
		if (name != null) {
			importData((INamedElement) iMsg, name);
		}
		
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			iMsg.setArgument(arg);
			iMsg.setReturnValue(returnValue);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "importData(IMessage) Exception: [" + iMsg.getName() + "] - [" + name + "] [" + arg + "] exeption: " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}

	// for combine fragment's operand
	public void importData (IInteractionOperand element, String str) {
		if (element == null || str == null || str == null) {
			return;
		}
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			element.setGuard(str);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "importData(IInteractionOperand) Exception: " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}
	
	// import data for Icomment
	public void importData (IComment element, String str) {
		if (element == null || str == null) {
			return;
		}
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			element.setName(str);
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "importData(IComment) Exception: " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}
	
	// import data for IInteractionUse
	public void importData (IInteractionUse element, String name, String ref) {
		if (element == null || name == null) {
			return;
		}
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			transaction.beginTransaction();
			element.setName(name);
			
			INamedElement refSeq = null;
			if (Utils.isEmpty(ref)) {
				// ignore, remove reference sequence
			}
			else {
				INamedElement[] sqs = getSequenceDiagrams();
				for (INamedElement sq : sqs) {
					if (ref.equals(Utils.trim(sq.getName()))) {
						refSeq = sq;
						break;
					}
				}
			}
			
			// set the sequence, if null -> unspecific
			if (refSeq == null) {
				Log.d(TAG, "importData(IInteractionUse) can not find out sq: " + ref);
			}
			element.setSequenceDiagram((ISequenceDiagram)refSeq);

			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "importData(IInteractionUse) Exception: " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
		}
	}
	
	boolean importDataForText(IDiagram dg, IPresentation pst, String value) {
		if (pst == null || value == null) {
			return false;
		}
		ITransactionManager transaction = mProjectAccessor.getTransactionManager();
		try {
			SequenceDiagramEditor sqEditor = mProjectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor();
			sqEditor.setDiagram(dg);
			transaction.beginTransaction();
			
			INodePresentation newTextNode = sqEditor.createText(value, ((INodePresentation) pst).getLocation());
			sqEditor.deletePresentation(pst);
			Map<String, String> args = new HashMap<>();
			args.put(PresentationPropertyConstants.Key.FILL_COLOR, pst.getProperty(PresentationPropertyConstants.Key.FILL_COLOR));
			args.put(PresentationPropertyConstants.Key.FONT_COLOR, pst.getProperty(PresentationPropertyConstants.Key.FONT_COLOR));
			newTextNode.setProperties(args);
			//pst.setLabel(value);
			
			transaction.endTransaction();
		} catch (Exception e) {
			Log.e(TAG, "importDataForText() Exception: " + e.getMessage());
			if (transaction.isInTransaction() == true) {
				transaction.endTransaction();
			}
			return false;
		}
		return true;
	}
	
	/* ===============================================================================================
	 * 					create Element/Model functions
	 * ===============================================================================================
	 */
	IClass createICLass (String name) {
		if (mProjectAccessor == null || name == null) {
			return null;
		}
        ITransactionManager transactionManager = mProjectAccessor.getTransactionManager();
        IModelEditorFactory modelEditorFactory = mProjectAccessor.getModelEditorFactory();
        IClass res = null;
        try {
            BasicModelEditor basicModelEditor = modelEditorFactory.getBasicModelEditor();
            transactionManager.beginTransaction();
            res = basicModelEditor.createClass(mProjectAccessor.getProject(), name);
            transactionManager.endTransaction();
        } catch (Exception e) {
        	Log.e(TAG, "createICLass() Exception: " + e.getMessage());
            transactionManager.abortTransaction();
        }	
        return res;
	}
	
	
	
}