package controllers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class UserInterface {
	
	public static Controller controller;
	public Display display;
	public Shell shell;

    public UserInterface(Display display) {
    	
    	this.display = display;
        controller = new Controller();
        shell = new Shell(display, SWT.SHELL_TRIM | SWT.CENTER);
        init();
    }

    @SuppressWarnings("unused")
    protected void init() {
        
        shell.setText("Api Migration Recommender");
        shell.setSize(600, 200);

        Rectangle bds = display.getBounds();

        Point p = shell.getSize();

        int nLeft = (bds.width - p.x) / 2;
        int nTop = (bds.height - p.y) / 2;

        shell.setBounds(nLeft, nTop, p.x, p.y);

        RowLayout layout = new RowLayout(1);
        layout.marginLeft = 50;
        layout.marginTop = 50;
        shell.setLayout(layout);

        // create text box to receive source API
        RowData sourceRowData = new RowData(150,25);
        Text sourceText = addTextBox("Source API", sourceRowData);

        // create text box to receive source API
        RowData targetRowData = new RowData(150,20);
        Text targetText = addTextBox("Target API", targetRowData);
        
        // create and add button to cancel
        RowData quitRowData = new RowData(80,30);
        Button quitBtn = addButton("Cancel", quitRowData);
        quitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.getDisplay().dispose();
                System.exit(0); 
            }
        }); 
        
        // create and add button to enter choices
        RowData okRowData = new RowData(80, 30);
        Button okBtn = addButton( "OK", okRowData);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	try {
					controller.generateRecommendations("place", "holder");
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ClientProtocolException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                shell.getDisplay().dispose();
                System.exit(0); 
            }
        }); 
    }
    
    protected void openShell() {

        shell.open();

        while (!shell.isDisposed()) {
          if (!display.readAndDispatch()) {
            display.sleep();
          }
        }
    }
    
    protected Text addTextBox(String label, RowData rowData) {
    	Text newTextBox = new Text(shell, 100);
    	newTextBox.setMessage(label);
    	newTextBox.setLayoutData(rowData);
    	return newTextBox;
    }
    
    protected Button addButton(String text, RowData rowData) {
    	Button newBtn = new Button(shell, SWT.PUSH);
        newBtn.setText(text);
        newBtn.setLayoutData(rowData);
		return newBtn;
    	
    }
    
    public void teardown() {
    	this.display.dispose();
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) throws ClientProtocolException, URISyntaxException, IOException {
    	
    	Display display = new Display();
        UserInterface ex = new UserInterface(display);
        ex.openShell();
        ex.teardown();
        controller.generateRecommendations("gson", "jackson");
        
        }
}
