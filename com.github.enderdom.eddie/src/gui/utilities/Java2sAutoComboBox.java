package gui.utilities;

import java.awt.event.ItemEvent;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class Java2sAutoComboBox extends JComboBox {
	  /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	private class AutoTextFieldEditor extends BasicComboBoxEditor {

	    private Java2sAutoTextField getAutoTextFieldEditor() {
	      return (Java2sAutoTextField) editor;
	    }
	    @SuppressWarnings("rawtypes")
	    AutoTextFieldEditor(List list) {
	      editor = new Java2sAutoTextField(list, Java2sAutoComboBox.this);
	    }
	  }
	
	  @SuppressWarnings("rawtypes")
	  public Java2sAutoComboBox(List list) {
	    isFired = false;
	    autoTextFieldEditor = new AutoTextFieldEditor(list);
	    setEditable(true);
	    setModel(new DefaultComboBoxModel(list.toArray()) {

	      /**
			 * 
			 */
	    	private static final long serialVersionUID = 1L;

	    	protected void fireContentsChanged(Object obj, int i, int j) {
	    		if (!isFired)
	    			super.fireContentsChanged(obj, i, j);
	    	}

	    });
	    setEditor(autoTextFieldEditor);
	  }

	  public boolean isCaseSensitive() {
	    return autoTextFieldEditor.getAutoTextFieldEditor().isCaseSensitive();
	  }

	  public void setCaseSensitive(boolean flag) {
	    autoTextFieldEditor.getAutoTextFieldEditor().setCaseSensitive(flag);
	  }

	  public boolean isStrict() {
	    return autoTextFieldEditor.getAutoTextFieldEditor().isStrict();
	  }

	  public void setStrict(boolean flag) {
	    autoTextFieldEditor.getAutoTextFieldEditor().setStrict(flag);
	  }
	  @SuppressWarnings("rawtypes")
	  public List getDataList() {
	    return autoTextFieldEditor.getAutoTextFieldEditor().getDataList();
	  }
	  @SuppressWarnings("rawtypes")
	  public void setDataList(List list) {
	    autoTextFieldEditor.getAutoTextFieldEditor().setDataList(list);
	    setModel(new DefaultComboBoxModel(list.toArray()));
	  }

	  void setSelectedValue(Object obj) {
	    if (isFired) {
	      return;
	    } else {
	      isFired = true;
	      setSelectedItem(obj);
	      fireItemStateChanged(new ItemEvent(this, 701, selectedItemReminder,
	          1));
	      isFired = false;
	      return;
	    }
	  }

	  protected void fireActionEvent() {
	    if (!isFired)
	      super.fireActionEvent();
	  }

	  private AutoTextFieldEditor autoTextFieldEditor;

	  private boolean isFired;

	}