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
 *Original project: https://github.com/Imran92/Java-UDP-Video-Stream-Server.git by Imram92<br>
 *Adapted by: Paulavelandiar, Parr0sky and srodriguezb1<br>
 */
public class JavaServer extends Thread
{
	public static InetAddress[] inet;
	public static int[] port;
	public int puerto;
	public static int i;
	static int count = 0;
	public static BufferedReader[] inFromClient;
	public static DataOutputStream[] outToClient;
	public static Canvas_Demo[] canv;
	public static void main(String[] args) throws Exception
	{
		JavaServer jv = new JavaServer(4321);
		JavaServer jv1 = new JavaServer(4322);
		//JavaServer jv2 = new JavaServer(4323);
		//JavaServer jv3 = new JavaServer(4324);
		canv=new Canvas_Demo[4];
		
		jv1.start();
		jv.start();
		
		//jv2.start();
		//jv3.start();
	}
	
	public JavaServer(int puerto) throws Exception 
	{		
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "c:/Program Files/VideoLAN/VLC");
		this.puerto=puerto;
		JavaServer.inet = new InetAddress[30];
		port = new int[30];
		
	}
	public void run() {
		try {
			int cont=0;
			ServerSocket welcomeSocket=null;
			int ssP=6782;
			while(cont<=3) {
				try {
					welcomeSocket = new ServerSocket(ssP);
					break;
				}catch(Exception e){
					ssP++;
					cont++;
					continue;
				}
				
			}
		
			Socket connectionSocket[] = new Socket[30];
			
			inFromClient = new BufferedReader[30];
			outToClient = new DataOutputStream[30];

			DatagramSocket serv = new DatagramSocket(puerto);
			
			byte[] buf = new byte[62000];

			DatagramPacket dp = new DatagramPacket(buf, buf.length);
			String vid="video.mp4";
			switch (puerto) {
			case 4321:
				vid="video.mp4";
				break;
			case 4322:
				vid="video2.mp4";
				break;
			case 4323:
				vid="video3.mp4";
				break;
			case 4324:
				vid="video4.mp4";
				break;
			default:
				break;
			}
			 canv[cont] = new Canvas_Demo(vid,cont+1);
			
			i = 0;

			while (true)
			{			
				serv.receive(dp);
				
				buf = "starts".getBytes();

				inet[i] = dp.getAddress();
				port[i] = dp.getPort();
				

				DatagramPacket dsend = new DatagramPacket(buf, buf.length, inet[i], port[i]);
				serv.send(dsend);

				Vidthread sendvid = new Vidthread(serv);
				
				connectionSocket[i] = welcomeSocket.accept();

				inFromClient[i] = new BufferedReader(new InputStreamReader(connectionSocket[i].getInputStream()));
				outToClient[i] = new DataOutputStream(connectionSocket[i].getOutputStream());
				outToClient[i].writeBytes("Connected: from Server\n");
				
				sendvid.start();
				
				i++;

				if (i == 30)
				{
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Vidthread extends Thread 
{
	int clientno;
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

				mybuf = rb.createScreenCapture(rc);

				img = new ImageIcon(mybuf);

				jleb.setIcon(img);
				jf.add(jleb);
				jf.repaint();
				jf.revalidate();

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				ImageIO.write(mybuf, "jpg", baos);
				
				outbuff = baos.toByteArray();

				for (int j = 0; j < num; j++) 
				{
					DatagramPacket dp = new DatagramPacket(outbuff, outbuff.length, JavaServer.inet[j],
							JavaServer.port[j]);
					
					soc.send(dp);
					baos.flush();
				}
				Thread.sleep(15);
			} 
			catch (Exception e) 
			{

			}
		}

	}

}

class Canvas_Demo
{
	// Create a media player factory
	public  MediaPlayerFactory mediaPlayerFactory;

	// Create a new media player instance for the run-time platform
	public  EmbeddedMediaPlayer mediaPlayer;

	public static JPanel panel;
	public static JPanel myjp;
	public static Canvas canvas;
	public static JFrame frame;
	public static JTextArea ta;
	public static JTextArea txinp;
	public static int xpos = 0, ypos = 0;
	String url = "D:\\DownLoads\\Video\\freerun.MP4";

	// Constructor
	public Canvas_Demo(String video,int pos) 
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
		frame = new JFrame("Reproductor"+pos);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(200*pos, 200*pos);
		frame.setSize(300, 260);
		frame.setAlwaysOnTop(true);

		mypanel.add(panel);
		frame.add(mypanel, BorderLayout.CENTER);
		frame.setVisible(true);
		xpos = frame.getX();
		ypos = frame.getY();

		// Playing the video

		mediaPlayer.playMedia(video);
	
		mypanel.revalidate();
		mypanel.repaint();
	}
}