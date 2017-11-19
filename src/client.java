//Anas Ahmad

import java.net.*;
import java.io.*; 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class client extends JFrame implements ActionListener
{
  
  private static final long serialVersionUID = 1L;
  private JTextField message;
  private JTextArea history;
  private JTextField user;
  private JTextArea AllUsers;
  private JPanel panel;
  private JButton sendToAll;
  private JButton sendButton;
  private boolean SentToAll;
  private static String name;

  // Network Items
  private boolean connected;
  private Socket echoSocket;
  private PrintWriter out;
  private PrintWriter out2;
  private BufferedReader in;
  
   // set up GUI
   public client(String name) throws IOException
   {
      super(name);
      getContentPane().setBackground(Color.BLACK);
      
      // get content pane and set its layout
      Container container = getContentPane();
      container.setLayout (new BorderLayout ());
      
      // set up the North panel
      JPanel upperPanel = new JPanel ();
      upperPanel.setBackground(new Color(0, 102, 153));
      upperPanel.setLayout (new GridLayout (1,2));
      JPanel lowerPanel = new JPanel ();
      lowerPanel.setLayout (new GridLayout (4,2));
      container.add (upperPanel, BorderLayout.NORTH);
      //container.add (lowerPanel, BorderLayout.SOUTH);
         
      // create buttons
      connected = false;
      
      
      JLabel label = new JLabel ("Username: ", JLabel.LEFT);
      upperPanel.add ( label );
      user = new JTextField ("");
      user.addActionListener( this );
      upperPanel.add( user );
      
      SentToAll = false;
                      
      history = new JTextArea ( 5, 30 );
      history.setBackground(Color.BLACK);
      history.setForeground(new Color(0, 102, 153));
      history.setEditable(false);
      JScrollPane scrollPane = new JScrollPane(history);
      
      
      AllUsers = new JTextArea ( 5, 10 );
      AllUsers.setBackground(new Color(0, 102, 153));
      AllUsers.setLineWrap(true);
      AllUsers.setEditable(false);
      scrollPane.setRowHeaderView(AllUsers);
      container.add( scrollPane, BorderLayout.CENTER);
      
      panel = new JPanel();
      panel.setBackground(new Color(0, 102, 153));
      getContentPane().add(panel, BorderLayout.SOUTH);
      panel.setLayout(new GridLayout(0, 1));
      message = new JTextField ("");
      message.setBackground(Color.WHITE);
      message.setColumns(4);
      panel.add(message);
      message.setHorizontalAlignment(SwingConstants.LEFT);
      message.addActionListener( this );
      message.setText("");
      
      sendButton = new JButton("Send Message");
      sendButton.addActionListener(this);
      sendButton.setEnabled(false);
      panel.add(sendButton);
      
      sendToAll = new JButton("Send to All");
      sendToAll.addActionListener(this);
      panel.add(sendToAll);
      
              
      gameTimer.start();
  
      doManageConnection();								//connecting the client to the server
      
      setSize( 500, 450 );
      setVisible( true );

	  out.println(name);								//sending name to the server

      
	  out2.println("INCOMINGCLIENTS");					//asking for users to be printed 
	  
	  
	  
	  addWindowListener(new java.awt.event.WindowAdapter() 
      {
   	    @Override
   	    public void windowClosing(java.awt.event.WindowEvent windowEvent) 
   	    {
   	        	
    		
    		out2.println("EXITING");
    		
   	        System.exit(0);
   	        
   	    }
   	});
      
      
   } // end CountDown constructor

   
   Timer gameTimer = new Timer(1000, new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			receiveMessage();	
		}
	});  
  
   
   public static void main( String args[] ) throws IOException
   { 
	   
      name = JOptionPane.showInputDialog(null, "Enter your name..");     
      client application = new client(name);
      
      
      application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
   }


   // handle button event
   public void actionPerformed( ActionEvent event )
    {
	   
	   	SentToAll = false;
	   	
	   
    	if ( connected && (event.getSource() == sendButton || event.getSource() == message || event.getSource() == user) )
    	{ 
    		doSendMessage();
        }
    	
    	else if(event.getSource() == sendToAll)
    	{
    		SentToAll = true;
    		doSendMessage();
    	}
    
    	
    }
   
  
   public void receiveMessage()
    {
    	String input;
    	int userNumber = 0;
    	
    	try 
    	{
    		if(in != null && in.ready())
    			{
    				if((input = in.readLine()) != null)    		
    				{
    					
    					if(input.equals("INCOMINGCLIENTS"))
    					{
    						AllUsers.setText("");
    						
    						if((input = in.readLine()) != null)
    							userNumber = Integer.parseInt(input);
    						
    						for(int i = 0; i < userNumber; i++)
    						{
    							if((input = in.readLine()) != null)
    								AllUsers.append(input + '\n');
    						}
    					}
    					
    					
    					else if(input.equals("") || (input.equals("\n")))
    					{}
    						
    					else
    						history.append(input + "\n");
    				}
    			}	
		} 
    	
    	catch (IOException e) 
    	{
			e.printStackTrace();
		}
    }
    
    public void doSendMessage()
    {
    
    		out.println(name + ": " + message.getText());							//send 'out' the message
    		
    		history.append(name + ": " + message.getText() + "\n");					//enter the message you sent in the chatbox
    		
    		
    		if(SentToAll == true)													//if send to all button is pressed
    			out2.println("SENDTOALL");
    		
    		else																
    			out2.println(user.getText());
    		
    		message.setText("");
 
    }
  
    
    public void doManageConnection()
    {
      if (connected == false)
      {
    	  String machineName = null;
    	  int portNum;
    	  try 
    	  {
    		  machineName = "127.0.0.1";
    		  portNum = 10000;//Integer.parseInt(portInfo.getText());
            
    		  echoSocket = new Socket(machineName, portNum);
            
    		  out = new PrintWriter(echoSocket.getOutputStream(), true);
    		  out2 = new PrintWriter(echoSocket.getOutputStream(), true);
            
    		  in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            
    		  sendButton.setEnabled(true);
    		  connected = true;
    	  } 
        
        catch (NumberFormatException e) 
        {
        	history.insert ( "Server Port must be an integer\n", 0);
        } 
        
        catch (UnknownHostException e) 
        {
            history.insert("Don't know about host: " + machineName , 0);
        } 
        
        catch (IOException e) 
        {
            history.insert ("Couldn't get I/O for "
                               + "the connection to: " + machineName , 0);
        }

      }
      else
      {
    	  try 
    	  {   
    		  out.close();
    		  in.close();
    		  echoSocket.close();
    		  sendButton.setEnabled(false);
    		  connected = false;
    	  }
        
    	  catch (IOException e) 
    	  {
    		  history.insert ("Error in closing down Socket ", 0);
    	  }
      }

        
    }

 } // end class EchoServer3




 
