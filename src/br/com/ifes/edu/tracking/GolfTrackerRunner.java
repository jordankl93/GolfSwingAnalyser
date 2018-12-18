package br.com.ifes.edu.tracking;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import br.com.ifes.edu.tracking.to.ResultadoDeteccao;
import br.com.ifes.edu.tracking.to.Taco;


public class GolfTrackerRunner {
	
	private VideoCapture videoCapture;
	
	HashMap<Integer,Taco> detectedClubs;
	List<Taco> clubSequence;
	HashMap<Integer,Taco> realClubs;
	List<Taco> realClubSequence;
	
	boolean close;
	int frame;
	boolean firstHandSeted;
	
	int vWidth;
	int vHeight;
	
	private Mat videoFrameAnterior;
	private Mat videoFrameAtual;
	private Mat videoFramePosterior;
	//private Mat videoFrameResult;
	private Mat imageToDraw;
	
	private JFrame jframe;
	private static JLabel vidpanel;
	
	private ClubExtraction clubExtraction;
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);		
		GolfTrackerRunner runner = new GolfTrackerRunner(args[0]);
		runner.start();				
	}
	
	
	public GolfTrackerRunner(String videoPath) {
		close = false;
		frame = 0;
		firstHandSeted = false;
		
		Point mao0 = new Point(0, 0);
		
		clubExtraction = new ClubExtraction();
				
		videoFrameAnterior = new Mat();
		videoFrameAtual = new Mat();
		videoFramePosterior = new Mat();
		//videoFrameResult = new Mat();
		imageToDraw = new Mat();
		
		detectedClubs = new HashMap<Integer,Taco>();
		clubSequence = new ArrayList<Taco>();
		realClubs = new HashMap<Integer,Taco>();
		realClubSequence = new ArrayList<Taco>();
		
		videoCapture = new VideoCapture();
		videoCapture.open(videoPath);
		if(videoCapture.isOpened()) {
			
			videoCapture.read(videoFrameAtual);
			videoCapture.read(videoFramePosterior);
			videoFrameAtual.copyTo(imageToDraw);
			
			vWidth = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH);
			vHeight = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
			
			jframe = new JFrame("Rastreando...");
		    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    jframe.setMinimumSize(new Dimension(vWidth, vHeight));
		    
		    
		    vidpanel = new JLabel();
		    jframe.setContentPane(vidpanel);
		    jframe.setVisible(true);
		    
		    
		    
		    updateImage(imageToDraw);
		    
		    jframe.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {}
				
				@Override
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub
					switch (e.getKeyChar()) {
					case 'n':
						processNextFrame();
						break;
					case 'c':
						if(frame == 0) {
							detectedClubs.clear();
							realClubs.clear();
							firstHandSeted = false;
							videoFrameAtual.copyTo(imageToDraw);
							updateImage(imageToDraw);
						}
						break;
					default:
						break;
					};
				}
			});
		    
		    vidpanel.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {}
				
				@Override
				public void mousePressed(MouseEvent e) {}
				
				@Override
				public void mouseExited(MouseEvent e) {}
				
				@Override
				public void mouseEntered(MouseEvent e) {}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					int actualHeight = vidpanel.getHeight();
				    int actualWidth = vidpanel.getWidth();
				    int x = e.getX() * vWidth / actualWidth;
				    int y = e.getY() * vHeight / actualHeight;
					if(firstHandSeted) {
						
						Point ponta0 = new Point(x, y);
						Taco t0 = new Taco(mao0, ponta0);
						realClubs.put(frame, t0);
						realClubSequence.add(t0);
						//firstHandSeted = false;
						if(frame == 0) {
							detectedClubs.put(frame, t0);
							clubSequence.add(t0);
						}						
						Imgproc.circle(imageToDraw, ponta0, 5, new Scalar(0, 0, 0));
						updateImage(imageToDraw);
					}
					else {
						mao0.x = x;
						mao0.y = y;
						firstHandSeted = true;
						Imgproc.circle(imageToDraw, mao0, 5, new Scalar(0, 0, 255));
						updateImage(imageToDraw);
					}
				}
			});
		}
	}
	
	public void processNextFrame() {
		frame++;
		System.out.println("frame: " + frame);
		if(frame < videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT) - 2) {
			videoFrameAtual.copyTo(videoFrameAnterior);
			videoFramePosterior.copyTo(videoFrameAtual);
			videoFrameAtual.copyTo(imageToDraw);
			videoCapture.read(videoFramePosterior);
			Taco taco0 = detectedClubs.get(0);
			Taco tacoAnterior = clubSequence.get(clubSequence.size()-1);
			Taco detectedTaco = clubExtraction.trackMotion(videoFrameAnterior, videoFrameAtual, videoFramePosterior, imageToDraw, taco0, tacoAnterior);
			if(detectedTaco != null) {
				clubSequence.add(detectedTaco);
				detectedClubs.put(frame, detectedTaco);
				System.out.println("Achou a ponta!");
			}
			if( realClubSequence.size() > 1 ) {
				for( int i = 1; i < realClubSequence.size(); i++) {
					Imgproc.line(imageToDraw, realClubSequence.get(i-1).getPonta(), realClubSequence.get(i).getPonta(), new Scalar(255,0,0), 2);
				}
			}
			if( clubSequence.size() > 1 ) {
				for( int i = 1; i < clubSequence.size(); i++) {
					Imgproc.line(imageToDraw, clubSequence.get(i-1).getPonta(), clubSequence.get(i).getPonta(), new Scalar(0,0,0), 2);
				}
			}
			updateImage(imageToDraw);			
		}
		else {
			if(detectedClubs.size() == realClubs.size()) {
				for( Integer nrFrame : detectedClubs.keySet() ) {
					System.out.println(String.format("Frame: %d; Pa: %s; Pr: %s; Err: %.2f",
							nrFrame,
							detectedClubs.get(nrFrame).getPonta(),
							realClubs.get(nrFrame).getPonta(), 
							ClubExtraction.euclideanDistance(detectedClubs.get(nrFrame).getPonta(), realClubs.get(nrFrame).getPonta())));
				}
			}
			close = true;
		}
		System.out.println("");
		
	}
	
	public void start() {
		while(!close) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
//	public void start() {
//		while(true) {
//			ResultadoDeteccao resultado = new ResultadoDeteccao();
//			Long countFrame = Long.valueOf(1);
//			
//			/* TODO : VAMOS CRIAR UM PONTO FIXO INICIAL POR ENQUANTO PARA ASSUIMIR QUE FOI SELECIONADO A PONTA DO TACO */
////			teste2.mp4
//			Point pontaMao = new Point(490, 408);
//			Point pontaTaco = new Point(477, 505);
//			
////			teste4.mp4
////			Point pontaMao = new Point(495, 358);
////			Point pontaTaco = new Point(532, 460);
//			
////			teste.mp4
////			Point pontaMao = new Point(730, 608);
////			Point pontaTaco = new Point(718, 750);
//			
//			
//			resultado.setPontaMao(pontaMao);
//			resultado.setPontoTaco(pontaTaco);
//			
//			//videoCapture.open("C:\\eclipseC++\\workplace\\GolfClubTracking\\teste2.mp4");
//			
//			if (videoCapture.isOpened()) {
//				
//	
//				while (videoCapture.get(1) < videoCapture.get(7) - 2) {
//			    	
//			    	videoCapture.read(videoFrameAnterior);
//					
//					try {
//						Thread.sleep(1);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					
//					videoCapture.read(videoFrameAtual);
//					
//					try {
//						Thread.sleep(1);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					
//					videoCapture.read(videoFramePosterior);
//					
//					resultado = clubExtraction.trackMotion(videoFrameAnterior, videoFrameAtual, videoFramePosterior, videoFrameResult, countFrame, resultado);
//					
//					countFrame++;
//					//updateImage(videoFrameResult);		       
//			    }
//				
//			} else {
//				System.out.println("Nao foi possivel reproduzir o video.");
//			}
//			
//		}		
//	}
	
	public static void updateImage(Mat matrix) {
		ImageIcon image = new ImageIcon(Mat2BufferedImage(matrix));
        vidpanel.setIcon(image);
        vidpanel.repaint();
    }
	
	 /*
     * Converts/writes a Mat into a BufferedImage.
     * 
     * @param matrix Mat of type CV_8UC3 or CV_8UC1
     * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
     */
	public static BufferedImage Mat2BufferedImage(Mat m) {

		MatOfByte mb = new MatOfByte();
        Imgcodecs.imencode(".jpg", m, mb);
        try {
             return ImageIO.read(new ByteArrayInputStream(mb.toArray()));
        } catch (IOException e) {
             e.printStackTrace();
             return null; // Error
        }        
	}
	
	/* Método alternativo -- se mostrou mais rápido que o outro*/
//	public static BufferedImage Mat2BufferedImage(Mat m) {
//		// Method converts a Mat to a Buffered Image
//		int type = BufferedImage.TYPE_BYTE_GRAY;
//		if (m.channels() > 1) {
//			type = BufferedImage.TYPE_3BYTE_BGR;
//		}
//		int bufferSize = m.channels() * m.cols() * m.rows();
//		byte[] b = new byte[bufferSize];
//		m.get(0, 0, b); // get all the pixels
//		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
//		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//		System.arraycopy(b, 0, targetPixels, 0, b.length);
//		return image;
//	}

}
