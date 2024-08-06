package SQDManagement;

import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.IPresentation;

import java.util.regex.Pattern;

import javax.naming.NameParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;

import java.util.Formatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

class ImportSeqDataController {
	private static String TAG = "ImportSeqDataController";
	private AstahGateway mGateway = null;

	private ImportDataHolder mHolder = null;
	private String mInputPath = null;

	public ImportSeqDataController(AstahGateway gw, String input) {
		mGateway = gw;
		mInputPath = input;
	}

	@Override
	protected void finalize() {

	}

	// ===========================================================================================
	// import function
	// ===========================================================================================

	private void importData(ILifeline ill, String originName) {
		if (originName == null) {
			Log.e(TAG, "importData(ILifeline): originName is null");
		}

		// check and detect name class or reference class.
		IClass base = ill.getBase();
		String name = originName;
		boolean needSearchForClass = false;
		if (originName.startsWith("@") == true) {
			String namePattern = "@(.*)";
			Pattern p = Pattern.compile(namePattern);
			Matcher m = p.matcher(originName);
			if (m.matches()) {
				name = Utils.trim(m.group(1));
				needSearchForClass = true;
			}
		}

		if (Utils.isEmpty(name)) {
			Log.e(TAG, "importData(ILifeline) the name is empty. please check");
			return;
		}

		if (needSearchForClass) {
			// find the class with 'name'
			INamedElement[] classes = mGateway.getClasses();
			IClass newBase = null;
			if (classes != null) {
				for (INamedElement cl : classes) {
					if (name.equals(Utils.trim(cl.getName()))) {
						if (newBase != null) {
							Log.e(TAG, "importData(ILifeline): there are miltiple class name: " + name);
						} else {
							newBase = (IClass) cl;
						}
					}
				}
			}

			// if can not find out any reference class, create new one
			if (newBase == null) {
				Log.d(TAG, "importData(ILifeline): can not find the class [" + name + "] create new one");
				newBase = mGateway.createICLass(name);
			}

			// set the base class and remove the name
			if (newBase != null) {
				mGateway.setBaseForILifeline(ill, newBase);
				mGateway.importData((INamedElement) ill, "");
			} else {
				Log.e(TAG, "importData(ILifeline): can not create new IClass");
			}
		} else {
			// remove the base class
			if (base != null) {
				mGateway.setBaseForILifeline(ill, null);
			}

			// set the name of lifeline
			mGateway.importData((INamedElement) ill, name);
		}
	}

	// the function will be line "funcName(params1, param2)
	private void importData(IMessage iMsg, String value) {
		if (Utils.isEmpty(value)) {
			Log.e(TAG, "importData(IMessage): value is empty");
			return;
		}
		String typeChar = "a-zA-Z0-9\\s.,:<>-_+!#$%&*=";
		String iMsgPattern = "([" + typeChar + "]+)\\((.*)\\)([" + typeChar + "]*)";
		
		if (iMsg.getIndex() == null) {
			Log.d(TAG, "importData(IMessage): target maybe is reply message [" + value + "]");
			mGateway.importData((INamedElement) iMsg, value);
		} else if (value.startsWith("@")) {
			INamedElement target = iMsg.getTarget();
			if (target == null || target instanceof ILifeline == false) {
				Log.e(TAG, "importData(IMessage): target is null or invalid [" + value + "]");
				return;
			}

			IClass baseTarget = ((ILifeline) target).getBase();
			if (baseTarget == null) {
				Log.e(TAG, "importData(IMessage): base is null, please check your config again [" + value + "]");
				return;
			}
			String iMsgRefPattern = "@" + iMsgPattern;
			Pattern p = Pattern.compile(iMsgRefPattern);
			Matcher m = p.matcher(value);
			if (m.matches()) {
				String refFuncName = Utils.trim(m.group(1));
				String arg = Utils.trim(m.group(2));
				String returnValue = Utils.trim(m.group(3));

				// find the Iopreation the refFuncName is refered to.
				IOperation[] ops = baseTarget.getOperations();
				IOperation refOp = null;
				for (IOperation op : ops) {
					if (refFuncName.equals(Utils.trim(op.getName()))) {
						refOp = op;
						break;
					}
				}
				if (refOp != null) {
					mGateway.importData(iMsg, refOp, arg, returnValue);
				} else {
					Log.e(TAG, "importData(IMessage): can not find reference func [" + value + "]");
				}
				// mGateway.importData(iMsg, func, param);
			} else {
				Log.e(TAG, "importData(IMessage): reference pattern for message is not matched [" + value + "]");
			}
		} else {
			Pattern p = Pattern.compile(iMsgPattern);
			Matcher m = p.matcher(value);
			if (m.matches()) {
				String func = Utils.trim(m.group(1));
				String param = Utils.trim(m.group(2));
				String returnValue = Utils.trim(m.group(3));
				mGateway.importData(iMsg, (IOperation) null, null, null);
				mGateway.importData(iMsg, func, param, returnValue);
			} else {
				Log.e(TAG, "importData(IMessage): pattern for message is not matched [" + value + "]");
			}
		}
	}

	private void importData(ICombinedFragment iCbf, String value) {
		if (iCbf == null) {
			return;
		}
		String arr[] = value.split("[\\n\\r]");
		if (arr == null || arr.length < 2) {
			Log.e(TAG, "importData(ICombinedFragment) invalid format: " + value);
			return;
		}
		String namePattern = "name:([a-zA-Z0-9\\s.,-_+]*)";
		Pattern p = Pattern.compile(namePattern);
		Matcher m = p.matcher(arr[0]);
		if (m.matches() == false) {
			Log.e(TAG, "importData(ICombinedFragment) name's pattern is not match str=[" + arr[0] + "]");
			return;
		}
		String name = Utils.trim(m.group(1));
		mGateway.importData((INamedElement) iCbf, name);

		if ("guards:".equals(Utils.trim(arr[1])) == false) {
			Log.e(TAG, "importData(ICombinedFragment) guard's pattern is not match str=[" + arr[1] + "]");
		}
		String guardPattern = "@(.*)";
		p = Pattern.compile(guardPattern);
		IInteractionOperand iItOp[] = iCbf.getInteractionOperands();
		if (iItOp.length == arr.length - 2) {
			for (int i = 0; i < iItOp.length; i++) {
				m = p.matcher(arr[i + 2]);
				if (m.matches()) {
					mGateway.importData(iItOp[i], Utils.trim(m.group(1)));
				} else {
					Log.e(TAG, "importData(ICombinedFragment) guard's pattern is not match: " + arr[i + 2]
							+ " => ignore import this element");
					break;
				}
			}
		} else {
			Log.e(TAG, "importData(ICombinedFragment) number of guards is missmatch with .xlxs");
		}
	}

	private void importData(IComment iCm, String content) {
		mGateway.importData(iCm, content);
	}

	private void importData(IInteractionUse iItU, String content) {
		String arr[] = content.split("[\\n\\r]");
		if (arr == null || arr.length < 2) {
			Log.e(TAG,
					"importData(IInteractionUse) err at " + iItU.getName() + " " + (arr == null ? "null" : arr.length));
			return;
		}
		String namePattern = "name:(.*)";
		String refPattern = "(reference:|@)(.*)";
		String name = "", ref = "";

		Pattern p = Pattern.compile(namePattern);
		Matcher m = p.matcher(arr[0]);
		if (m.matches() == true) {
			name = m.group(1);
		} else {
			Log.e(TAG,
					"importData(IInteractionUse) error at name's pattern: " + arr[0] + " => ingnore update this item");
			return;
		}

		p = Pattern.compile(refPattern);
		m = p.matcher(arr[1]);
		if (m.matches() == true) {
			ref = m.group(2);
		} else {
			Log.e(TAG,
					"importData(IInteractionUse) error at name's pattern: " + arr[1] + " => ingnore update this item");
			return;
		}

		mGateway.importData(iItU, Utils.trim(name), Utils.trim(ref));
	}

	private static boolean isKeyValid(String key) {
		if (Utils.isEmpty(key)) {
			return false;
		}
		if (key.startsWith("#") == false) {
			return false;
		}
		return true;
	}

	private void importData(ISequenceDiagram sq) throws Exception {
		String sqName = Utils.trim(sq.getName());
		if (mHolder.isSeqExisted(sqName) == false) {
			Log.d(TAG, "importData(ISequenceDiagram) sequence(" + sqName + ")'s info is not existed");
			return;
		}
		ImportDataHolder.SequenceInfo seqInfo = mHolder.getSeqInfo(sqName);
		if (seqInfo == null) {
			Log.e(TAG, "importData(ISequenceDiagram) can't find the seqInfo for '" + sqName + "'");
			return;
		}
		
		Log.d(TAG, "importData(ISequenceDiagram) start import ISequenceDiagram={" + sqName + "}");

		// update LifeLine
		ArrayList<IPresentation> lifeLines = mGateway.getLifeLines(sq);
		Log.d(TAG, "importData(ISequenceDiagram) start import lifeLines=" + lifeLines);
		for (IPresentation ipst : lifeLines) {
			ILifeline item = (ILifeline) ipst.getModel();
			String tag = Utils.trim(item.getName());
			if (isKeyValid(tag) == false) {
				tag = mGateway.getDDTagValue(item);
			}
			if (isKeyValid(tag) == false)
				continue;

			String value = seqInfo.get(tag);
			if (value != null) {
				importData(item, value);
				// because clone dont's have a ddtag, so add it for later udpate
				if (mGateway.hasDDTag(item) == false) {
					mGateway.addDDTag(item, tag);
				}
			} else {
				Log.d(TAG, "importData(ISequenceDiagram) lifeLine: ignore item tag(" + tag + ") value(" + value + ")");
			}
		}

		// update Message
		ArrayList<IPresentation> messages = mGateway.getMessage(sq);
		Log.d(TAG, "importData(ISequenceDiagram) start import IMessages=" + messages);
		for (IPresentation ipst : messages) {
			IMessage item = (IMessage) ipst.getModel();
			String tag = Utils.trim(item.getName());
			if (isKeyValid(tag) == false) {
				tag = mGateway.getDDTagValue(item);
			}
			if (isKeyValid(tag) == false)
				continue;

			String value = seqInfo.get(tag);
			if (value != null) {
				importData(item, value);
				// because clone dont's have a ddtag, so add it for later udpate
				if (mGateway.hasDDTag(item) == false) {
					mGateway.addDDTag(item, tag);
				}
			} else {
				Log.d(TAG, "importData(ISequenceDiagram) IMessage: ignore item:  tag(" + tag + ") value(" + value + ")");
			}
		}

		// update combinefragment
		ArrayList<IPresentation> cbfs = mGateway.getCombineFragments(sq);
		Log.d(TAG, "importData(ISequenceDiagram) start import ICombinedFragment=" + cbfs);
		for (IPresentation ipst : cbfs) {
			ICombinedFragment item = (ICombinedFragment) ipst.getModel();
			String tag = Utils.trim(item.getName());
			if (isKeyValid(tag) == false) {
				tag = mGateway.getDDTagValue(item);
			}
			if (isKeyValid(tag) == false)
				continue;

			String value = seqInfo.get(tag);
			if (value != null) {
				importData(item, value);
				// because clone dont's have a ddtag, so add it for later udpate
				if (mGateway.hasDDTag(item) == false) {
					mGateway.addDDTag(item, tag);
				}
			} else {
				Log.d(TAG, "importData(ISequenceDiagram) ICombinedFragment: ignore item tag(" + tag + ") value(" + value + ")");
			}
		}

		// update comment
		ArrayList<IPresentation> cms = mGateway.getComments(sq);
		Log.d(TAG, "importData(ISequenceDiagram) start import IComment=" + cms);
		for (IPresentation ipst : cms) {
			IComment item = (IComment) ipst.getModel();
			String tag = Utils.trim(item.getBody());
			if (isKeyValid(tag) == false) {
				tag = mGateway.getDDTagValue(item);
			}
			if (isKeyValid(tag) == false)
				continue;

			String value = seqInfo.get(tag);
			if (value != null) {
				importData(item, value);
				// because clone dont's have a ddtag, so add it for later udpate
				if (mGateway.hasDDTag(item) == false) {
					mGateway.addDDTag(item, tag);
				}
			} else {
				Log.d(TAG, "importData(ISequenceDiagram) IComment: ignore item tag(" + tag + ") value(" + value + ")");
			}
		}

		// update IInteractionUse
		ArrayList<IPresentation> iIttU = mGateway.getInteractionUse(sq);
		Log.d(TAG, "importData(ISequenceDiagram) start import IInteractionUse=" + iIttU);
		for (IPresentation ipst : iIttU) {
			IInteractionUse item = (IInteractionUse) ipst.getModel();
			String tag = Utils.trim(item.getName());
			if (isKeyValid(tag) == false) {
				tag = mGateway.getDDTagValue(item);
			}
			if (isKeyValid(tag) == false)
				continue;

			String value = seqInfo.get(tag);
			if (value != null) {
				importData(item, value);
				// because clone dont's have a ddtag, so add it for later udpate
				if (mGateway.hasDDTag(item) == false) {
					mGateway.addDDTag(item, tag);
				}
			} else {
				Log.d(TAG, "importData(ISequenceDiagram) IInteractionUse: ignore item tag(" + tag + ") value(" + value + ")");
			}
		}

		// update text
		ArrayList<IPresentation> textPresentationList = mGateway.getTexts(sq);
		HashMap<String, String> keysDB = null;
		String textDbFileName = mGateway.getProjectName() + "_" + sqName;
		keysDB = getTextDB(textDbFileName);
		String newDbInfo = "";
		Log.d(TAG, "importData(ISequenceDiagram) start import TEXT keysDB=" + keysDB + " Texts=" + textPresentationList);
		for (IPresentation textPresentation : textPresentationList) {
			String curText = Utils.trim(textPresentation.getLabel());
			if (Utils.isEmpty(curText) == true) {
				Log.d(TAG, "importData(ISequenceDiagram) TEXT: some text is empty");
				continue;
			}
			
			String key = null;
			String oldMd5Str = null;
			if (curText.startsWith("#") || keysDB == null) {
				key = curText;
			} else {
				oldMd5Str = md5sum2(curText);
				key = keysDB.get(oldMd5Str);
				if (Utils.isEmpty(key)) {
					Log.e(TAG, "importData(ISequenceDiagram) TEXT: can not get key from DB(" + curText + ")");
				}
			}
			
			String value = seqInfo.get(key);
			if (Utils.isEmpty(value)) {
				Log.e(TAG, "importData(ISequenceDiagram) TEXT: key=" + key + " is not exited.");
				continue;
			}
			
			Log.d(TAG, "importData(ISequenceDiagram) TEXT: key=" + key + " value=" + value);
			boolean isOk = mGateway.importDataForText(sq, textPresentation, value);
			if (isOk) {
				String newMd5Str = md5sum2(value);
				newDbInfo += newMd5Str + "\n" + key + "\n";
			}
			else {
				// keep previous md5 and value
				if (oldMd5Str != null) {
					newDbInfo += oldMd5Str + "\n" + key + "\n";
				}
			}
		}
		if (Utils.isEmpty(newDbInfo) == false) {
			UpdateTextDB(textDbFileName, newDbInfo);
		}
	}
	
	private boolean importNewSequenceDiagram(INamedElement tempalateSequence, String newSequenceName) throws Exception {
		if (Utils.isEmpty(newSequenceName) || tempalateSequence == null) {
			Log.e(TAG, "importNewSequenceDiagram(): name or sequence is null/empty");
			return false;
		}

		mGateway.importData(tempalateSequence, newSequenceName); // set the name of the sequence
		importData((ISequenceDiagram) tempalateSequence);
		return true;
	}
	
	// ===========================================================================================
	// handling text data base and get template function
	// ===========================================================================================
	public static String md5sum2(final String plaintext) {
		try {
			final Formatter formatter = new Formatter();
			final MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(plaintext.getBytes(StandardCharsets.UTF_16LE));
			for (final byte b : digest.digest())
				formatter.format("%02x", b);
			return formatter.toString();
		} catch (final NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
	}
	
	public static void UpdateTextDB(String fileName, String newDbInfo) throws IOException {
		if (Utils.isEmpty(fileName)) {
			Log.e(TAG, "UpdateTextDB() file name is empty");
			return;
		}
		String dirPath = System.getenv("LOCALAPPDATA") + "\\astah\\";
		String filePath = dirPath + fileName + ".txt";
		File dir = new File(dirPath);
		if (dir.exists() == false) {
			Files.createDirectories(Paths.get(dirPath));
		}
		Log.d(TAG, "UpdateTextDB() path=" + filePath);
		File file = new File(filePath);
		FileWriter writer = new FileWriter(file);
		writer.write(newDbInfo);
		writer.close();
	}
	
	public static HashMap<String, String> getTextDB(String fileName) throws IOException {
		if (Utils.isEmpty(fileName)) {
			Log.e(TAG, "getTextDB() file name is empty");
			return null;
		}
		String filePath = System.getenv("LOCALAPPDATA") + "\\astah\\" + fileName + ".txt";
		File file = new File(filePath);
		if (file.exists() == false) {
			Log.d(TAG, "getTextDB() file is not existed: " + filePath);
			return null;
		}
		HashMap<String, String> ret = new HashMap<>();
		Scanner reader = new Scanner(file);
		Log.d(TAG, "getTextDB() path=" + filePath);
		while (reader.hasNextLine()) {
			String md5Key = Utils.trim(reader.nextLine());
			String value = Utils.trim(reader.nextLine());
			Log.d(TAG, "getTextDB() md5=" + md5Key + " value=" + value);
			ret.put(md5Key, value);
		}
		reader.close();
		return ret;
	}

	private ArrayList<INamedElement> getListOfTemplateSequenceDiagram(INamedElement[] seqDgs, String templateName) {
		if (seqDgs == null || Utils.isEmpty(templateName)) {
			return null;
		}
		ArrayList<INamedElement> ret = new ArrayList<>();
		for (INamedElement item : seqDgs) {
			if (item instanceof ISequenceDiagram) {
				String name = Utils.trim(item.getName());
				if (!Utils.isEmpty(name) && name.startsWith(templateName) && !name.equals(templateName)) {
					ret.add(item);
				}
			}
		}
		return ret;
	}

	// ===========================================================================================
	// run function
	// ===========================================================================================
	public void run() throws Exception {
		Log.d(TAG, "run() +++++++++++ Start Import ++++++++++++++");
		if (mGateway == null || mInputPath == null) {
			Log.e(TAG, "run() import ERROR: mGateway=" + mGateway + " inputPath=" + mInputPath);
			return;
		}

		try {
			mHolder = new ImportDataHolder(mInputPath);
		} catch (Exception e) {
			Log.e(TAG, "run() exception at reading data: " + e.getMessage());
			mHolder = null;
		}

		if (mHolder != null) {
			INamedElement[] seqDgs = mGateway.getSequenceDiagrams();
			if (seqDgs == null || seqDgs.length == 0) {
				Log.d(TAG, "run() your project doesn't have any of sequence diagram");
				throw new Exception("There is no any sequence diagram in this project");
			}
			
			IDiagram curDiagram = mGateway.getOpenedDiagram();
			ArrayList<INamedElement> subTemplates = null; // store templates which are cloned before importing
			ArrayList<String> newSequenceInfos = null; // store the name of new sequence
			Log.d(TAG, "run() current sequence: " + curDiagram);
			
			// get list of template and new sequences
			if (curDiagram != null && curDiagram instanceof ISequenceDiagram) {
				String seqName = Utils.trim(curDiagram.getName());
				// define template as #template name#
				if (!Utils.isEmpty(seqName) && seqName.startsWith("#") && seqName.endsWith("#")) {
					subTemplates = getListOfTemplateSequenceDiagram(seqDgs, seqName);
					newSequenceInfos = mHolder.getNewImportedSequence(seqDgs);
					Log.d(TAG, "run() NewInfo=" + newSequenceInfos + " subTemplate=" + subTemplates);
				}
			}

			// import all existing sequence diagrams
			Log.d(TAG, "run()======= start import existing sequence=======");
			for (int i = 0; i < seqDgs.length; i++) {
				importData((ISequenceDiagram) seqDgs[i]);
			}

			// import new sequence info
			if (newSequenceInfos != null && subTemplates != null) {
				int maxAddedSequence = subTemplates.size();
				if (newSequenceInfos.size() < maxAddedSequence) {
					maxAddedSequence = newSequenceInfos.size();
				}
				Log.d(TAG, "run()======= start import new sequence=======");
				Log.d(TAG, "run() NewInfo=" + newSequenceInfos.size() + " subTemplate=" + subTemplates.size()
						+ " maxImported=" + maxAddedSequence);
				for (int i = 0; i < maxAddedSequence; i++) {
					Log.d(TAG, "run() import Sequence Diagram: {" + newSequenceInfos.get(i) + "}");
					importNewSequenceDiagram(subTemplates.get(i), newSequenceInfos.get(i));
				}

				// print out any sequence infor which is not imported
				for (int i = maxAddedSequence; i < newSequenceInfos.size(); i++) {
					Log.d(TAG, "run() WARNING: new sequence can not be imported: [" + newSequenceInfos.get(i) + "]");
				}
			}

			mGateway.save();
			mGateway.showMessage("IMPORT... done!\n");
			Log.d(TAG, "run() =====All DONE=====\n");
		} else {
			mGateway.showMessage("ERROR: when reading your data. please check again");
		}
	}
}
