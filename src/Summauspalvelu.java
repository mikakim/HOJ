import java.io.*;
import java.net.*;


public class Summauspalvelu extends Thread {
	
	private int index;
	private boolean onYhteys;
	private ServerSocket ss;
	private Socket soketti;
	private SummauspalveluidenHallinta hallinta;
	
	public Summauspalvelu(int index, int portti, SummauspalveluidenHallinta hallinta) throws IOException {
		super();
		this.index = index;
		this.hallinta = hallinta;
		onYhteys = false;
		ss = new ServerSocket(portti);
		soketti = null;
	}
	
	public void run() {
		try {
			soketti = ss.accept();
			onYhteys = true;
			InputStream inputS = soketti.getInputStream();
			ObjectInputStream objectIn = new ObjectInputStream((inputS));
			while (onYhteys) {
				if (!hallinta.lisaaLuku(index, objectIn.readInt())) {				
					suljeYhteys();
					break;
				} else {
									
				}
			}
		} catch (Exception e) {
			
		}
		System.out.println(index + ". summauspalvelu suljettu!");
	}
	public void suljeYhteys() {
		try {
			soketti.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		onYhteys = false;
	}
}
