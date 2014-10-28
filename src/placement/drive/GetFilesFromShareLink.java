package placement.drive;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;


public class GetFilesFromShareLink {

	private static final String requestURL =  "https://docs.google.com/a/iiitd.ac.in/uc?export=download&id=";
	private final String dir;
	
	public GetFilesFromShareLink(String dir){
		this.dir = dir;
	}

	public void SaveFile(String ID, String fileName, boolean isBtech) {
		try{
			HttpURLConnection con =(HttpURLConnection) new URL(requestURL + ID).openConnection();
			con.setRequestMethod("GET");
			if(isBtech)
				Files.copy(con.getInputStream(), new java.io.File(dir + fileName + ".pdf").toPath());
			else
				Files.copy(con.getInputStream(), new java.io.File(dir + fileName + ".pdf").toPath());

		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/** Email of the Service Account */
	private static final String SERVICE_ACCOUNT_EMAIL = 
			"292882852430-mp1l9lqkr2fl0c56rg5u0eehafqsv5k3@developer.gserviceaccount.com";

	/** Path to the Service Account's Private Key file */
	private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH = 
			"URL-947be66e22b9.p12";

	/**
	 * Build and returns a Drive service object authorized with the service accounts.
	 *
	 * @return Drive service object that is ready to make requests.
	 */
	public static Drive getDriveService() throws GeneralSecurityException,
	IOException, URISyntaxException {
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		GoogleCredential credential = new GoogleCredential.Builder()
		.setTransport(httpTransport)
		.setJsonFactory(jsonFactory)
		.setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
		.setServiceAccountScopes(Arrays.asList(DriveScopes.DRIVE))
		.setServiceAccountPrivateKeyFromP12File(
				new java.io.File(SERVICE_ACCOUNT_PKCS12_FILE_PATH))
				.build();
		Drive service = new Drive.Builder(httpTransport, jsonFactory, null)
		.setHttpRequestInitializer(credential)
		.setApplicationName("GetFiles").build();
		return service;
	}

	/**
	 * Download a file's content.
	 * 
	 * @param service Drive API service instance.
	 * @param file Drive File instance.
	 * @return InputStream containing the file's content if successful,
	 *         {@code null} otherwise.
	 */
	private static InputStream downloadFile(Drive service, String URL) {
		if (URL != null && URL.length() > 0) {
			try {
				HttpResponse resp =
						service.getRequestFactory().buildGetRequest(new GenericUrl(URL))
						.execute();
				return resp.getContent();
			} catch (IOException e) {
				// An error occurred.
				e.printStackTrace();
				return null;
			}
		} else {
			// The file doesn't have any content stored on Drive.
			return null;
		}
	}
}