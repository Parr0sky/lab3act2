


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;

import javax.swing.*;

import com.sun.jna.NativeLibrary;
import com.sun.jna.platform.win32.WinUser.POINT;

import java.nio.file.Files;
import java.time.Duration;

import uk.co.caprica.vlcj.binding.LibVlc;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.x.LibXUtil;
//import uk.co.caprica.vlcj.runtime.windows.WindowsRuntimeUtil;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

/**
 *
 * @author imran
 */
public class JavaServer 
{
	public static InetAddress[] inet;
	public static int[] port;
	public static int i;
	static int count = 0;
	public static BufferedReader[] inFromClient;
	public static DataOutputStream[] outToClient;
	
	public static void main(String[] args) throws Exception
	{
		JavaServer jv = new JavaServer();
	}
	
	public JavaServer() throws Exception 
	{
		
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "c:/Program Files/VideoLAN/VLC");

		JavaServer.inet = new InetAddress[30];
		port = new int[30];

		ServerSocket welcomeSocket = new ServerSocket(6782);
	
		Socket connectionSocket[] = new Socket[30];
		
		inFromClient = new BufferedReader[30];
		outToClient = new DataOutputStream[30];

		DatagramSocket serv = new DatagramSocket(4321);


		byte[] buf = new byte[62000];


		DatagramPacket dp = new DatagramPacket(buf, buf.length);


		Canvas_Demo canv = new Canvas_Demo();

		i = 0;

		
		SThread[] st = new SThread[30];
		while (true)
		{
			System.out.println(serv.getPort());
			
			serv.receive(dp);
			
			System.out.println(new String(dp.getData()));
			buf = "starts".getBytes();


			inet[i] = dp.getAddress();
			port[i] = dp.getPort();
			

			DatagramPacket dsend = new DatagramPacket(buf, buf.length, inet[i], port[i]);
			serv.send(dsend);

			Vidthread sendvid = new Vidthread(serv);

			System.out.println("waiting\n ");
			connectionSocket[i] = welcomeSocket.accept();
			System.out.println("connected " + i);

			inFromClient[i] = new BufferedReader(new InputStreamReader(connectionSocket[i].getInputStream()));
			outToClient[i] = new DataOutputStream(connectionSocket[i].getOutputStream());
			outToClient[i].writeBytes("Connected: from Server\n");

			
			st[i] = new SThread(i);
			st[i].start();
			
			if(count == 0)
			{
				Sentencefromserver sen = new Sentencefromserver();
				sen.start();
				count++;
			}

			System.out.println(inet[i]);
			sendvid.start();

			i++;

			if (i == 30) {
				break;
			}
		}
	}
}

class Vidthread extends Thread {

	int clientno;
	// InetAddress iadd = InetAddress.getLocalHost();
	JFrame jf = new JFrame("scrnshots before sending");
	JLabel jleb = new JLabel();

	DatagramSocket soc;

	Robot rb = new Robot();

	byte[] outbuff = new byte[62000];

	BufferedImage mybuf;
	ImageIcon img;
	Rectangle rc;
	
	int bord = Canvas_Demo.panel.getY() - Canvas_Demo.frame.getY();

	public Vidthread(DatagramSocket ds) throws Exception 
	{
		soc = ds;

		System.out.println(soc.getPort());
		jf.setSize(500, 400);
		jf.setLocation(500, 400);
		jf.setVisible(true);
	}

	public void run() 
	{
		while (true) 
		{
			try 
			{

				int num = JavaServer.i;

				rc = new Rectangle(new Point(Canvas_Demo.frame.getX() + 8, Canvas_Demo.frame.getY() + 27),
						new Dimension(Canvas_Demo.panel.getWidth(), Canvas_Demo.frame.getHeight() / 2));

				// System.out.println("another frame sent ");

				mybuf = rb.createScreenCapture(rc);

				img = new ImageIcon(mybuf);

				jleb.setIcon(img);
				jf.add(jleb);
				jf.repaint();
				jf.revalidate();
				// jf.setVisible(true);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				ImageIO.write(mybuf, "jpg", baos);
				
				outbuff = baos.toByteArray();

				for (int j = 0; j < num; j++) {
					DatagramPacket dp = new DatagramPacket(outbuff, outbuff.length, JavaServer.inet[j],
							JavaServer.port[j]);
					//System.out.println("Frame Sent to: " + JavaServer.inet[j] + " port: " + JavaServer.port[j]
						//	+ " size: " + baos.toByteArray().length);
					soc.send(dp);
					baos.flush();
				}
				Thread.sleep(15);

				// baos.flush();
				// byte[] buffer = baos.toByteArray();
			} 
			catch (Exception e) 
			{

			}
		}

	}

}

class Canvas_Demo {

	// Create a media player factory
	private MediaPlayerFactory mediaPlayerFactory;

	// Create a new media player instance for the run-time platform
	private EmbeddedMediaPlayer mediaPlayer;

	public static JPanel panel;
	public static JPanel myjp;
	private Canvas canvas;
	public static JFrame frame;
	public static JTextArea ta;
	public static JTextArea txinp;
	public static int xpos = 0, ypos = 0;
	String url = "D:\\DownLoads\\Video\\freerun.MP4";

	// Constructor
	public Canvas_Demo() 
	{

		// Creating a panel that while contains the canvas
		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel mypanel = new JPanel();
		mypanel.setLayout(new GridLayout(1, 1));

		// Creating the canvas and adding it to the panel :
		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		panel.add(canvas, BorderLayout.CENTER);
	

		// Creation a media player :
		mediaPlayerFactory = new MediaPlayerFactory();
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
		CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
		mediaPlayer.setVideoSurface(videoSurface);

		// Construction of the jframe :
		frame = new JFrame("Reproductor");
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(200, 0);
		frame.setSize(300, 260);
		frame.setAlwaysOnTop(true);

		mypanel.add(panel);
		frame.add(mypanel, BorderLayout.CENTER);
		frame.setVisible(true);
		xpos = frame.getX();
		ypos = frame.getY();

		// Playing the video

		//System.out.println("video.mp4");
		mediaPlayer.playMedia("video.mp4");
	
	
		mypanel.revalidate();
		mypanel.repaint();
	}
}

class SThread extends Thread {

	public static String clientSentence;
	int srcid;
	BufferedReader inFromClient = JavaServer.inFromClient[srcid];
	DataOutputStream outToClient[] = JavaServer.outToClient;

	public SThread(int a) {
		srcid = a;
	}

	public void run() {
		while (true) {
			try {

				clientSentence = inFromClient.readLine();
				// clientSentence = inFromClient.readLine();

				System.out.println("From Client " + srcid + ": " + clientSentence);
				Canvas_Demo.ta.append("From Client " + srcid + ": " + clientSentence + "\n");
				
				for(int i=0; i<JavaServer.i; i++)
				{
                    
                    if(i!=srcid)
                        outToClient[i].writeBytes("Client "+srcid+": "+clientSentence + '\n');	//'\n' is necessary
                }
				
				Canvas_Demo.myjp.revalidate();
				Canvas_Demo.myjp.repaint();

					} catch (Exception e) {
			}

		}
	}
}

class Sentencefromserver extends Thread {
	
	public static String sendingSentence;
	
	public Sentencefromserver() {

	}

	public void run() {

		while (true) {

			try {

				if(sendingSentence.length()>0)
				{
					for (int i = 0; i < JavaServer.i; i++) {
						JavaServer.outToClient[i].writeBytes("From Server: "+sendingSentence+'\n');
						
					}
					sendingSentence = null;
				}

			} catch (Exception e) {

			}
		}
	}
}