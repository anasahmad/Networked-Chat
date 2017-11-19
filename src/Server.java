//Anas Ahmad
//NETWORKED CHAT....
//there's an issue with receiving a message, you'd have to press the send button to receive as well.....


import java.net.*;
import java.util.ArrayList;
import java.io.*; 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame implements ActionListener{
  
	
	private static final long serialVersionUID = 1L;
	
	// server lists used for identification of clients
	private static ArrayList<Socket> clientPorts = new ArrayList<Socket>();		//client ports to identify for sending stuff
	private static ArrayList<String> clientNames = new ArrayList<String>();		//client names

	
	// GUI items
	private JButton ssButton;
	private JLabel machineInfo;
	JLabel portInfo;
	JTextArea history;
  	JTextArea clientList;
  	JTextArea messageTo;

  	// Network Items
  	boolean serverContinue;
  	ServerSocket serverSocket;

	private boolean running;


  	public Server()
  	{
  			super( "Main Server" );
	  		
	  		// get content pane and set its layout
	  		Container container = getContentPane();
	  		container.setLayout( new FlowLayout() );

	      // create buttons
	
	  		String machineAddress = null;
	  		try
	  		{  
	  			InetAddress addr = InetAddress.getLocalHost();
	  			machineAddress = addr.getHostAddress();
	  		}
	  		catch (UnknownHostException e)
	  		{
	  			machineAddress = "127.0.0.1";
	  		}
	  		machineInfo = new JLabel (machineAddress + "\n");
	  		container.add( machineInfo );
	  		portInfo = new JLabel (" Not Listening ");
	  		container.add( portInfo );
	
	  		container.add ( new JLabel ("Chat: ", JLabel.LEFT) );
	  		history = new JTextArea ( 10, 40 );
	  		history.setEditable(false);
	  		container.add( new JScrollPane(history) );
	      
	  		container.add ( new JLabel ("Msg to: ", JLabel.LEFT) );
	  		messageTo = new JTextArea ( 10, 40 );
	  		messageTo.setEditable(false);
	  		container.add( new JScrollPane(messageTo) );
	      
	  		container.add ( new JLabel ("Users Connected: ", JLabel.LEFT) );
	  		clientList = new JTextArea ( 10, 40 );
	  		clientList.setEditable(false);
	  		container.add( new JScrollPane(clientList) );
      
	      
	  		new ConnectionThread (this);
	  		setSize( 500, 600 );
	  		setVisible( true );
	
	   	} // end CountDown constructor

  	
	   	public static void main( String args[] )
	   	{ 
	   		Server application = new Server(); 
	   		application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	   	}

	   
	   	//client name getter
	   	public static ArrayList<String> getClientNames()
	   	{   
	   		return clientNames;
	   	}
	   
	   	public static void setClientNames(String temp)
	   	{
	   		clientNames.add(temp);	   
	   	}   
	   
	   	//client port getter
	   	public static ArrayList<Socket> getClientPorts()
	   	{
	   		return clientPorts;
	   	}
	   
	   	public static void setClientPorts(Socket temp)
	   	{  
	   		clientPorts.add(temp);
	   	}


	    public void actionPerformed( ActionEvent event )
	    {
	    	if (running == false)
	    	{
	    		new ConnectionThread (this);
	    	}
	    	
	    	else
	    	{
	    		serverContinue = false;
	    		ssButton.setText ("Start Listening");
	    		portInfo.setText (" Not Listening ");
	    	}
	    }
	
	
	 } // end class server
	
	
	class ConnectionThread extends Thread
	{
		Server gui;
	 
		public ConnectionThread (Server es3)
		{
			gui = es3;
			start();
		}
	   
		public void run()
		{
			gui.serverContinue = true;
	     
			try 
			{	 
				gui.serverSocket = new ServerSocket(10000); 
				gui.portInfo.setText("Listening on Port: " + gui.serverSocket.getLocalPort());
	      
				System.out.println ("Connection Socket Created");
				try 
				{ 
					while (gui.serverContinue)
					{
						System.out.println ("Waiting for Connection");
						new CommunicationThread (gui.serverSocket.accept(), gui); 
					}
				} 
				catch (IOException e) 
				{ 
					System.err.println("Accept failed."); 
					System.exit(1); 
				} 
			} 
			
			catch (IOException e) 
			{ 
				System.err.println("Could not listen on port: 10008."); 
				System.exit(1); 
			} 
	     
			finally
			{
				try 
				{
					gui.serverSocket.close(); 
				}
	       
				catch (IOException e)
				{	 
					System.err.println("Could not close port: 10008."); 
					System.exit(1); 
				} 
			}
		}
	 }

	
	class CommunicationThread extends Thread
	{ 
		//private boolean serverContinue = true;
		private Socket clientSocket;
		private Server gui;
		boolean DoneSendToAll;
	
		public CommunicationThread (Socket clientSoc, Server ec3) throws IOException
		{
			clientSocket = clientSoc;
			gui = ec3;
			start();	    
		}

		public void run()
		{
			System.out.println ("New Communication Thread Started");
			boolean checkSocket;
	    
			String inputMessage;
			String inputUsers;
	    
			try 
			{	 
				checkSocket = false;
				PrintWriter out;
				PrintWriter out2;					//sending size;
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	    	
				out = new PrintWriter(clientSocket.getOutputStream(), true);
	    	
				ArrayList<Socket> tempPorts = Server.getClientPorts();
				ArrayList<String> tempNames = Server.getClientNames(); 
	    	

				for(Socket s: tempPorts)								//check if the client is already in the list the we have
				{
					if(s.getPort() == clientSocket.getPort())
	    			checkSocket = true;	
				}
	    	
				if(checkSocket == false)									//if the client doesn't exist in the list we have already
				{ 
					Server.setClientPorts(clientSocket);							//add the client to the list

					if((inputMessage = in.readLine()) != null)					//read the first input, which would be the name of the client
					{													//then add it to the clients name list.
						Server.setClientNames(inputMessage);						//add client names to the server gui, to check
					}
				}
			
				
				
				gui.clientList.setText("");
	      
				for(String s: tempNames)
				{
					gui.clientList.insert(s + "\n", 0); 	
				}
	       	      
				int indexUser;
				int realIndex = 0;
				boolean containsUser;// = false;
	       
	        	while ((inputMessage = in.readLine()) != null) 
	            { 
	        		
					

	        		DoneSendToAll = false;
	        		
	        		if(inputMessage.equals("INCOMINGCLIENTS"))
	        		{		
	        			int size = tempNames.size();
	        			
	        			
	        			
	        			for(Socket s: tempPorts)
	        			{
	        				out = new PrintWriter(s.getOutputStream(), true);
	        				out2 = new PrintWriter(s.getOutputStream(), true);

	        				out.println("INCOMINGCLIENTS");
	        				
	        				out2.println(size);
	        				for(String g: tempNames)
	        				{
	        					out.println(g);
	        				}
	        			}
	        			
	        			out = new PrintWriter(clientSocket.getOutputStream(), true);
	        		}
	        		
	        		else
	        		{
	        			
	        			if((inputUsers = in.readLine()) == null)
		        			break;
		     
	        		
	        		gui.messageTo.insert(inputUsers + "\n", 0);
	        		gui.history.append (inputMessage + "\n");
	        		
	        		containsUser = false;
	        		indexUser = 0;
	        		for(String s: tempNames)						//check if the username given my the other user to send a message
	    				{												//exists or not.
					
	    					if(s.equals(inputUsers))
	    					{
	    						containsUser = true;
	    						realIndex = indexUser;
	       						break;
	    					}
	    					indexUser++;
	    				}
	        		
	        		
	        		
	        		
	        		
	        		
	        		
	        		if(inputUsers.equals(""))
	        		{
	        			JOptionPane.showMessageDialog(new JFrame(), "Please enter a User... ", "Dialog",
	        			        JOptionPane.ERROR_MESSAGE);
	        			
	        			out.println();	
	        		}
	        		    		
	        			
	        		else if(inputUsers.equals("SENDTOALL"))				//the inputUsers received is 'SENDTOALL' then send to all users.
	        		{
	        			
	        			gui.history.insert(inputMessage, 0);
	        			
	        			for(Socket s: tempPorts)
	        			{
	        				if(s.getPort() != clientSocket.getPort())
	        				{
	        					out = new PrintWriter(s.getOutputStream(), true);	//prints to all clients one by one except the sender
	        					out.println(inputMessage);								//sends the output
	        				}
	        			}
	        			
	        			out.println();
	        		}
	        		
	        		else if(inputUsers.equals("EXITING"))
	        		{
	        			Server.getClientNames().remove(inputMessage);
	        			out.println("");
	        		}
	        		
	        		else if(containsUser == true)
	        		{
	        			if(Server.getClientPorts().get(realIndex) == clientSocket)
	        			{
	        			out.println();	
	        			}	
	        			
	        			else
	        			{
	        			out = new PrintWriter(Server.getClientPorts().get(realIndex).getOutputStream(), true);
	        			out.println(inputMessage);
	        			}
	        		}
	        		
	        		else if(containsUser == false)
	        		{
	        			gui.history.append ("This user isn't available.\n");
	        			out.println();
	        		}
	        		
	        		else if (inputMessage.equals("Bye.")) 
	        			break; 
	
	        		else if (inputMessage.equals("End Server.")) 
	        			gui.serverContinue = false; 
	        			
	        		}
	        	} 
	        
	        	out.close(); 
	        	in.close(); 
	        	clientSocket.close(); 
	        } 
	    
			catch (IOException e) 
	        { 
				System.err.println("Problem with Communication Server");
				//System.exit(1); 
	        } 
	    }
	} 
