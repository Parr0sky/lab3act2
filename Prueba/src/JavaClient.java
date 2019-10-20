/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

/**
 *
 * 
 */
public class JavaClient 
{
	public static DatagramSocket ds;

	public static void main(String[] args) throws Exception 
	{
		JavaClient javaClient = new JavaClient(4321);
	}
	
	public JavaClient(int port) throws Exception
	{
		ds = new DatagramSocket();

		byte[] init = new byte[62000];
		init = "givedata".getBytes();

		InetAddress addr = InetAddress.getLocalHost();

		DatagramPacket dp = new DatagramPacket(init,init.length,addr,port);

		ds.send(dp);

		DatagramPacket rcv = new DatagramPacket(init, init.length);

		ds.receive(rcv);
		System.out.println(new String(rcv.getData()));

		Vidshow vd = new Vidshow();
		vd.start();

		String modifiedSentence;

		InetAddress inetAddress = InetAddress.getLocalHost();

		Socket clientSocket = new Socket(inetAddress, 6782);
		DataOutputStream outToServer =	new DataOutputStream(clientSocket.getOutputStream());

		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		outToServer.writeBytes("Thanks man\n");

		CThread write = new CThread(inFromServer, outToServer, 0);
		CThread read = new CThread(inFromServer, outToServer, 1);

		write.join();
		read.join();
		clientSocket.close();
	}
}


class Vidshow extends Thread  implements ActionListener  
{

	JFrame jf = new JFrame();
	public static JPanel jp = new JPanel(new GridLayout(1,1));
	JPanel opciones = new JPanel(new GridLayout(2,1));
	JPanel opcionesInt = new JPanel(new GridLayout(1,3));
	
	JLabel jl = new JLabel();
	boolean rep = true;
	
	public static JTextArea ta,tb;
	JTextField txtPuerto = new JTextField();

	byte[] rcvbyte = new byte[62000];

	DatagramPacket dp = new DatagramPacket(rcvbyte, rcvbyte.length);
	BufferedImage bf;
	ImageIcon imc;


	public Vidshow() throws Exception 
	{
		
		jf.setSize(300, 260);
		jf.setTitle("Cliente");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		jf.setLayout(new BorderLayout());
		jf.setVisible(true);
		
		jp.add(jl);
		JButton bot=new JButton("Pausar/Reproducir");
		bot.addActionListener(this);
		bot.setActionCommand("PAUSAR/REP");
		
		JButton ok =new JButton("OK");
		ok.addActionListener(this);
		ok.setActionCommand("OK");
		
		opcionesInt.add(new JLabel("Puerto"));
		opcionesInt.add(txtPuerto);
		opcionesInt.add(ok);
	
		opciones.add(bot);
		opciones.add(opcionesInt);
		
		jf.add(opciones,BorderLayout.SOUTH);
		
		jf.add(jp, BorderLayout.CENTER);
		
	}

	@Override
	public void run() {

		try {
			System.out.println("got in");
			do
			{
				if(rep==true)
				{
					System.out.println("doing");
					System.out.println(JavaClient.ds.getPort());

					JavaClient.ds.receive(dp);
					System.out.println("received");
					ByteArrayInputStream bais = new ByteArrayInputStream(rcvbyte);

					bf = ImageIO.read(bais);

					if (bf != null) {
						//jf.setVisible(true);
						imc = new ImageIcon(bf);
						jl.setIcon(imc);
						jp.add(jl);
						jf.add(jp);
						Thread.sleep(15);
					}
					jf.revalidate();
					jf.repaint();

				}
			} while (true);

		} catch (Exception e)
		{
			System.out.println("couldnt do it");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("PAUSAR/REP"))
		{
			rep=!rep;
		}
		
		if(e.getActionCommand().equals("OK"))
		{
			try 
			{
				JavaClient nuevoCliente = new JavaClient(Integer.parseInt(txtPuerto.getText()));
			} 
			catch (Exception e1) 
			{
				e1.printStackTrace();
			} 
		}
	}
}

class CThread extends Thread {

	BufferedReader inFromServer;
	Button sender = new Button("Send Text");
	DataOutputStream outToServer;
	public static String sentence;
	int RW_Flag;

	public CThread(BufferedReader in, DataOutputStream out, int rwFlag) {
		inFromServer = in;
		outToServer = out;
		RW_Flag = rwFlag;
		if(rwFlag == 0)
		{
			
			sender.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					sentence = Vidshow.tb.getText();
					Vidshow.ta.append("From myself: "+sentence+"\n");
					try{
						outToServer.writeBytes(sentence + '\n');
					}
					catch(Exception E)
					{

					}
					Vidshow.tb.setText(null);
				}
			});
		}
		start();
	}

	public void run() {
		String mysent;
		try {
			while (true) {
				if (RW_Flag == 0) {
					if(sentence.length()>0)
					{

						Vidshow.ta.append(sentence+"\n");
						Vidshow.ta.setCaretPosition(Vidshow.ta.getDocument().getLength());
						
						Vidshow.jp.revalidate();
						Vidshow.jp.repaint();
						outToServer.writeBytes(sentence + '\n');
						sentence = null;
						Vidshow.tb.setText(null);
					}
				} else if (RW_Flag == 1) {
					mysent = inFromServer.readLine();

					Vidshow.ta.append(mysent+"\n");
					Vidshow.ta.setCaretPosition(Vidshow.ta.getDocument().getLength());
					Vidshow.jp.revalidate();
					Vidshow.jp.repaint();



					System.out.println("From : " + sentence);
					sentence = null;

				}
			}
		} catch (Exception e) {
		}
	}}