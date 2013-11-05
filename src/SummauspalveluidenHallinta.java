import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class SummauspalveluidenHallinta extends Thread {

	private Summauspalvelu[] palvelut;
	private ArrayList<ArrayList<Integer>> saadutLuvut;
	ServerSocket ss;
	
	public static void main(String[] args) throws SocketException, IOException {
		SummauspalveluidenHallinta hallinta = new SummauspalveluidenHallinta();
		hallinta.start();
	}
		
	private Socket luoYhteys(int omaPortti, String osoite, int portti) throws Exception {
		Socket soketti = null;
		ss = new ServerSocket(omaPortti);
		for (int i = 0; i < 5; i++) {
			ss.setSoTimeout(50000);
			sendUDP(osoite, portti, Integer.toString(omaPortti));
			soketti = ss.accept();
			if (ss.isClosed()) {
				sendUDP(osoite, portti, Integer.toString(omaPortti));
			} else {
				break;
			}
		}
		if (ss.isClosed()) {
			// TODO: Nostaa poikkeuksen, yhteytt� ei muodostettu.
		}
		return soketti;
	}
	
	/**
	 * 
	 * @param address
	 * @param port
	 * @param message
	 * @throws IOException
	 * @throws SocketException
	 */
	public void sendUDP(String address, int port, String message) throws IOException, SocketException {
		InetAddress osoite = InetAddress.getByName(address);
		DatagramSocket soketti  = new DatagramSocket();
		byte[] sisalto = message.getBytes();
		DatagramPacket paketti = new DatagramPacket(sisalto, sisalto.length, osoite, port);
		soketti.send(paketti);
		soketti.close();
	}
	
	public void run() {
		try {
			Socket cs = luoYhteys(2000, "localhost", 3126);
			
			// Tiedostovirran luominen
			InputStream inputS = cs.getInputStream();
			OutputStream outputS = cs.getOutputStream();
			ObjectInputStream objectIn = new ObjectInputStream(inputS);
			ObjectOutputStream objectOut = new ObjectOutputStream(outputS);
			int lukumaara = objectIn.readInt();
			palvelut = new Summauspalvelu[lukumaara];
			saadutLuvut = new ArrayList<ArrayList<Integer>>(lukumaara);
			
			for (int i = 0; i < lukumaara; i++) {
				palvelut[i] = new Summauspalvelu(i, 2001+i, this);
				saadutLuvut.add(new ArrayList<Integer>());
			}
			for (int i = 0; i < lukumaara; i++) {
				palvelut[i].start();
			}
			for (int i = 0; i < lukumaara; i++) {
				objectOut.writeInt(2001+i);
				objectOut.flush();
			}
			while (true) {
				int input = objectIn.readInt();
				if (input == 1) {
					annaSummauspalvelijoilleAikaa();
					objectOut.writeInt(annaSumma());
					System.out.println("Summaa pyydetty: " + annaSumma());
				} else if (input == 2) {
					annaSummauspalvelijoilleAikaa();
					objectOut.writeInt(suurimmanSummanPalvelu());
					System.out.println("Eniten summannutta kysytty: " + suurimmanSummanPalvelu());
				} else if (input == 3) {
					annaSummauspalvelijoilleAikaa();
					System.out.println("Lukujen m��r�� pyydetty: " + lukujenMaara());
					objectOut.writeInt(lukujenMaara());
				} else {
					objectOut.writeInt(-1);
					System.out.println("Suljetaan sovellusta...");
					cs.close();
					suljeSummauspalvelut();
					break;
				}
				objectOut.flush();
			}
		} catch (Exception e1) {
			
		}
		for(int i = 0; i < palvelut.length; i++) {
			try {
				palvelut[i].join();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	
	/**
	 * Hallinta-s�ie antaa jokaiselle summauspalvelus�ikeelle yhden nanosekuntin suoritusaikaa
	 */
	private void annaSummauspalvelijoilleAikaa() {
		for(int i = 0; i < palvelut.length; i++) {
			try {
				palvelut[i].join(0, 1);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}
	
	private void suljeSummauspalvelut() {
		for (int i = 0; i < palvelut.length; i++) {
			palvelut[i].suljeYhteys();
		}
	}
	
	private int lukujenMaara() throws InterruptedException {
		int lukumaara = 0;
		for (int i = 0; i < annaSaadutLuvut().size(); i++) {
			lukumaara = lukumaara + annaSaadutLuvut().get(i).size();
		}
		return lukumaara;
	}
	
	private int suurimmanSummanPalvelu() throws InterruptedException {
		int[] summat = new int[annaSaadutLuvut().size()];
		for (int i = 0; i < annaSaadutLuvut().size(); i++) {
			for (int j = 0; j < annaSaadutLuvut().get(i).size(); j++) {
				summat[i] = summat[i] + annaSaadutLuvut().get(i).get(j);
			}
		}
		int suurimmanIndeksi = 0;
		for (int i = 1; i < summat.length; i++) {
			if (summat[suurimmanIndeksi] < summat[i])
				suurimmanIndeksi = i;
		}
		return suurimmanIndeksi+1;
	}
	
	private int annaSumma() throws InterruptedException {
		int summa = 0;
		for (int i = 0; i < annaSaadutLuvut().size(); i++) {
			for (int j = 0; j < annaSaadutLuvut().get(i).size(); j++) {
				summa = annaSaadutLuvut().get(i).get(j) + summa;
			}
		}
		return summa;
	}
	
	public boolean lisaaLuku(int indeksi, int luku) {
		if (luku == 0)
			return false;
		annaSaadutLuvut().get(indeksi).add(luku);
		System.out.println(indeksi + ". summauspalvelu lis�si luvun " + luku);
		return true;
	}
	
	public ArrayList<ArrayList<Integer>> annaSaadutLuvut() {
		return saadutLuvut;
	}
}
