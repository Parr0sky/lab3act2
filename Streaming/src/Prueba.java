import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.net.DatagramSocket;

import javax.swing.*;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class Prueba 
{
	public static void main(String[] args) 
	{	
		
		/**
		 * Creación de la ventana donde proyectar
		 */
		JFrame f = new JFrame();
		f.setLocation(100, 100);
		f.setSize(1000, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		
		/**
		 * Creación de la instancia de Canvas que usaremos para proyectar
		 */
		Canvas c = new Canvas();
		c.setBackground(Color.BLACK);
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
		p.add(c);
		f.add(p);
		
		/**
		 * Leemos el video
		 */
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:/Program Files/VideoLAN/VLC");
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		
		/**
		 * Inicializar Media Player
		 */
		MediaPlayerFactory mpf = new MediaPlayerFactory();
		
		/**
		 * Control de interacciones
		 */
		EmbeddedMediaPlayer emp = mpf.newEmbeddedMediaPlayer(new Win32FullScreenStrategy(f));
		emp.setVideoSurface(mpf.newVideoSurface(c));
		
		emp.setEnableKeyInputHandling(false);
		
		String file = "video.mp4";
		
		/**
		 * Preparar lectura
		 */
		emp.prepareMedia(file);
		
		/**
		 * Leer el archivo
		 */
		emp.play();
		
	}
}
