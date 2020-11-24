package htmlEditor;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.io.*;

public class Controller {
    private View view;
    private HTMLDocument document;
    private File currentFile;

    public Controller(View view) {
        this.view = view;
    }

    public void init(){
        createNewDocument();
    }

    public void exit(){
        System.exit(0);
    }

    public HTMLDocument getDocument() {
        return document;
    }

    public static void main(String[] args ){
        View view = new View();
        Controller controller = new Controller(view);
        view.setController(controller);
        view.init();
        controller.init();
    }

    public void resetDocument(){
        if(document!=null)
        document.removeUndoableEditListener( view.getUndoListener());
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        document = (HTMLDocument) htmlEditorKit.createDefaultDocument();
        document.addUndoableEditListener(view.getUndoListener());
        view.update();
    }

    public void setPlainText(String text){
        resetDocument();
        StringReader stringReader = new StringReader(text);
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        try {
            htmlEditorKit.read(stringReader, document, 0);
        } catch (IOException e) {
            ExceptionHandler.log(e);
        } catch (BadLocationException e) {
            ExceptionHandler.log(e);
        }
        stringReader.close();
    }

    public String getPlainText(){
        StringWriter stringWriter = new StringWriter();
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        try {
            htmlEditorKit.write(stringWriter, document, 0, document.getLength());
        } catch (IOException e) {
            ExceptionHandler.log(e);
        } catch (BadLocationException e) {
            ExceptionHandler.log(e);
        }
        return stringWriter.toString();
    }

    public void createNewDocument() {
        view.selectHtmlTab();
        resetDocument();
        view.setTitle("HTML редактор");
        view.resetUndo();
        currentFile = null;

    }

    public void openDocument() {
        view.selectHtmlTab();
        JFileChooser jFileChooser = new JFileChooser();
        HTMLFileFilter htmlFileFilter = new HTMLFileFilter();
        jFileChooser.setFileFilter(htmlFileFilter);
        if(jFileChooser.showOpenDialog(view)==JFileChooser.APPROVE_OPTION){

            try {
                resetDocument();
                currentFile = jFileChooser.getSelectedFile();

                view.setTitle(currentFile.getName());
                FileReader fileReader = new FileReader(currentFile);
                HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
                htmlEditorKit.read(fileReader, document, 0);
                fileReader.close();
            } catch (IOException | BadLocationException e) {
                ExceptionHandler.log(e);
            }
            view.resetUndo();

            }
            }


    public void saveDocumentAs() {
        view.selectHtmlTab();
        JFileChooser jFileChooser = new JFileChooser();
        HTMLFileFilter htmlFileFilter = new HTMLFileFilter();
        jFileChooser.setFileFilter(htmlFileFilter);
        if(jFileChooser.showSaveDialog(view)==JFileChooser.APPROVE_OPTION){

            try {
                currentFile = jFileChooser.getSelectedFile();
                view.setTitle(currentFile.getName());
                FileWriter fileWriter = new FileWriter(currentFile);

            HTMLEditorKit htmlEditorKit = new HTMLEditorKit();

                htmlEditorKit.write(fileWriter, document, 0, document.getLength());
                fileWriter.close();
            } catch (IOException e) {
                ExceptionHandler.log(e);
            } catch (BadLocationException e) {
                ExceptionHandler.log(e);
            }

        }

    }


    public void saveDocument() {
        if(currentFile==null) saveDocumentAs();
        else{try {
            view.selectHtmlTab();
            FileWriter fileWriter = null;

                fileWriter = new FileWriter(currentFile);

            HTMLEditorKit htmlEditorKit = new HTMLEditorKit();

                htmlEditorKit.write(fileWriter, document, 0, document.getLength());
                fileWriter.close();}
             catch (IOException e) {
                ExceptionHandler.log(e);
            } catch (BadLocationException e) {
                ExceptionHandler.log(e);
            }

        }
    }

}
