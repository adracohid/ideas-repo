package es.us.isa.ideas.repo.gdrive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Copy;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.Drive.Files.Update;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.primitives.Bytes;

import es.us.isa.ideas.repo.DummyAuthenticationManagerDelegate;
import es.us.isa.ideas.repo.IdeasRepo;

public class DriveQuickstart {
	public static String userAuthenticated() {
		DummyAuthenticationManagerDelegate authDelegate = new DummyAuthenticationManagerDelegate("usuariodeprueba608");
		IdeasRepo.setAuthManagerDelegate(authDelegate);
		return authDelegate.getAuthenticatedUserId();
	}

	private static final Logger LOGGER = Logger.getLogger(DriveQuickstart.class.getName());
	private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	// Primero hay que loguearse

	// Despues se coge los tokens de la carpeta del usuario logueado
	// private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final String TOKENS_DIRECTORY_PATH = "src/main/resources/" + userAuthenticated();

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static void main(String[] args) throws GeneralSecurityException, IOException {
		list();
		// folderList();
		// getFilesByFolderId("17QI8SXAjZntG0J_ne9wnC3yi8cuIfqZm");
		// getFoldersByFolderId("");
		// upload();
		// download("1u49m9v44r-MsNucNwDUV8aEiMRMKArX-");
		// createFolder();
		// insertFileInFolder();
		// createFolderInFolder();
		// getFoldersByRootDirectory();
		// getFileByName("New directory");
		// moveFilesBetweenFolders();
		// delete("1iLpbU5-8GsVqe_HDUK1CgNGODIwbWl_9");
		// deleteFolder();
		/*
		 * uploadFile("csv.csv"); uploadFile("R.r"); uploadFile("html.html");
		 * uploadFile("json.json"); uploadFile("text2.txt");
		 */
		// getFileByName("datos2.txt", "project1", "workspace4", "yo");

		// getWorkspaceByName("workspace4", "yo");
		// getFilesByFolderId("1HdO_fSaJ3pTmxfihhZdW3ZHE5lPfbYuf");

		// copiar workspace1 a workspace2
		// moveFileToFolder("1MHHTqoCY8ml74rzPnxQzAaXZ2jlAnz6Q","1xU0EXZ2q3t8F9Rp-oy0W4W_5-c6WOH14");
		// copyFileToFolder("1u5lAsYWK8tJXYKFSwNRUjvZ0UWHOjzc3","1JlPapv9sghJuy1KrGGKIEIcNIK7nBb5J");

		writeFile(uploadFile("text.txt").getId(), "Sample text");

	}

	public static void writeFile(String id, String msg) throws IOException, GeneralSecurityException {
		// Hay que comprobar que el fichero existe
		File f = driveService().files().get(id).setFields("name, parents").execute();
		String res = "";
		res=download(id)+msg;

		// 3º Creamos el buffer de escritura
		ByteArrayInputStream bais = new ByteArrayInputStream(res.getBytes());
		InputStreamContent content = new InputStreamContent("text/plain", bais);
		// 4º Creamos un fichero nuevo donde se va a añadir los datos
	
		System.out.println("Name: " + f.getName());
		File copy=driveService().files().create(new File().setName(f.getName()), content).setFields("id").execute();
		//Movemos el nuevo fichero a la carpeta del archivo de origen
		System.out.println("Carpeta: " + f.getParents().get(0));

		moveFileToFolder(copy.getId(), f.getParents().get(0));


		// Finalmente eliminamos el fichero de origen
		delete(id);
	}

	public static void writeFileAsByte(String id, byte[] msg) throws IOException, GeneralSecurityException {

		// Hay que comprobar que el fichero existe
		File f = driveService().files().get(id).setFields("name, parents").execute();
		// 1º Copiamos el contenido del fichero
		String s = "";
		s = s + download(id);
		// 2º Añadimos al contenido los datos a escribir
		byte[] b = Bytes.concat(s.getBytes(), msg);

		// 3º Creamos el buffer de escritura
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		InputStreamContent content = new InputStreamContent("text/plain", bais);
		// 4º Creamos un fichero nuevo donde se va a añadir los datos
		System.out.println("Nombre: "+f.getName());
		File copy=driveService().files().create(new File().setName(f.getName()), content).setFields("id").execute();
		//Movemos el nuevo fichero a la carpeta del archivo de origen
		moveFileToFolder(copy.getId(), f.getParents().get(0));
		// Finalmente eliminamos el fichero de origen
		delete(id);

	}

	// NO SE PUEDE COPIAR CARPETAS
	public static void copyFileToFolder(String fileId, String folderId) throws IOException, GeneralSecurityException {

		// 1º Se hace una copia de ese fichero
		File copy = driveService().files().copy(fileId, new File()).execute();

		// 2º Se mueve esa copia a la carpeta destino
		moveFileToFolder(copy.getId(), folderId);

	}

	// Tambien sirve para mover carpetas
	public static void moveFileToFolder(String fileId, String folderId) throws IOException, GeneralSecurityException {
		// 1º Obtiene la carpeta donde esta el fichero (parents)
		Get f = driveService().files().get(fileId);
		// Set fields es para OBTENER EL CAMPO QUE TU QUIERAS
		f.setFields("parents, id");
		File fi = f.execute();
		System.out.println("File id: " + fi.getId());

		String parents = String.join(",", fi.getParents());
		System.out.println("Parents: " + parents);

		// 2º Actualiza el fichero añadiendo como parent la carpeta destino y borrando
		// la carpeta origen
		Update update = driveService().files().update(fileId, new File());
		update.setFields("id, parents");
		update.setAddParents(folderId);
		update.setRemoveParents(parents);

		File file = update.execute();
		System.out.println("Copy id: " + file.getId());

	}

	public static File uploadFile(String name) throws IOException, GeneralSecurityException {
		File fileMetadata = new File();
		String type = null;

		if (name.contains(".txt")) {
			type = "text/plain";
		}
		// TODO
		// Si type==null, solo se puede poner las extensiones anteriores
		fileMetadata.setName(name);
		java.io.File filePath = new java.io.File("src/main/resources/files/" + name);
		FileContent mediaContent = new FileContent(type, filePath);
		File file = driveService().files().create(fileMetadata, mediaContent).setFields("id").execute();
		return file;

	}

	public static File getDirectoryByName(String name, String project, String workspace, String owner)
			throws IOException, GeneralSecurityException {
		File res;
		String pageToken = null;
		File file = getProjectByName(project, workspace, owner);
		// Si no existe el proyecto no devuelve nada
		if (file == null) {
			res = null;
		} else {
			do {
				FileList result = DriveQuickstart.driveService().files().list()
						// Busca el fichero dentro del proyecto
						.setQ("'" + file.getId()
								+ "' in parents and mimeType='application/vnd.google-apps.folder' and name='" + name
								+ "'")
						.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken)
						.execute();
				// Si no encuentra el fichero devuelve directamente null
				try {
					res = result.getFiles().get(0);
				} catch (IndexOutOfBoundsException e) {
					System.out.println("No se ha encontrado el fichero");
					res = null;
				}
				pageToken = result.getNextPageToken();
			} while (pageToken != null);
		}
		return res;
	}

	public static File getFileByName(String name, String project, String workspace, String owner)
			throws IOException, GeneralSecurityException {
		File res;
		String pageToken = null;
		File file = getProjectByName(project, workspace, owner);
		// Si no existe el proyecto no devuelve nada
		if (file == null) {
			res = null;
		} else {
			do {
				FileList result = DriveQuickstart.driveService().files().list()
						// Busca el fichero dentro del proyecto
						.setQ("'" + file.getId()
								+ "' in parents and mimeType!='application/vnd.google-apps.folder' and name='" + name
								+ "'")
						.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken)
						.execute();
				// Si no encuentra el fichero devuelve directamente null
				try {
					res = result.getFiles().get(0);
				} catch (IndexOutOfBoundsException e) {
					System.out.println("No se ha encontrado el fichero");
					res = null;
				}
				pageToken = result.getNextPageToken();
			} while (pageToken != null);
		}

		return res;
	}

	public static File getProjectByName(String name, String workspace, String owner)
			throws IOException, GeneralSecurityException {
		File res;
		String pageToken = null;
		File file = getWorkspaceByName(workspace, owner);
		// Si no existe el workspace no devuelve nada
		if (file == null) {
			res = null;
		} else {
			do {
				FileList result = DriveQuickstart.driveService().files().list()
						// Busca el proyecto dentro del workspace
						.setQ("'" + file.getId()
								+ "' in parents and mimeType='application/vnd.google-apps.folder' and name='" + name
								+ "'")
						.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken)
						.execute();
				// Si no encuentra el proyecto devuelve directamente null
				try {
					res = result.getFiles().get(0);
				} catch (IndexOutOfBoundsException e) {
					System.out.println("No se ha encontrado el proyecto");
					res = null;
				}
				pageToken = result.getNextPageToken();
			} while (pageToken != null);
		}
		return res;
	}

	public static File getWorkspaceByName(String name, String owner) throws IOException, GeneralSecurityException {
		File res;
		String pageToken = null;
		do {
			FileList result = DriveQuickstart.driveService().files().list()
					// Busca la carpeta dentro del directorio raiz,
					// comprueba que sea una carpeta
					// y que tenga el mismo nombre que el usuario
					.setQ("'" + getRepoFolder(owner).getId()
							+ "' in parents and mimeType='application/vnd.google-apps.folder' and name='" + name + "'")
					.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			// Si no encuentra el workspace devuelve directamente null
			try {
				res = result.getFiles().get(0);
			} catch (IndexOutOfBoundsException e) {
				res = null;
				System.out.println("No se ha encontrado el workspace");
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		return res;

	}

	public static File getRepoFolder(String owner) throws IOException, GeneralSecurityException {
		File res = new File();
		String pageToken = null;
		do {
			FileList result = DriveQuickstart.driveService().files().list()
					// Busca la carpeta dentro del directorio raiz,
					// comprueba que sea una carpeta
					// y que tenga el mismo nombre que el usuario
					.setQ("'root' in parents and mimeType='application/vnd.google-apps.folder' and name='" + owner
							+ "'")
					.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();

			try {
				res = result.getFiles().get(0);
			} catch (IndexOutOfBoundsException e) {
				res = null;
				System.out.println("No se ha encontrado el repositorio");
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		return res;
	}

	private static void deleteFolder() throws IOException, GeneralSecurityException {
		Drive service = driveService();
		String pageToken = null;
		File f = new File();
		do {
			FileList result = service.files().list().setQ("name contains 'New directory'")
					.setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			f = result.getFiles().get(0);
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		driveService().files().delete(f.getId()).execute();

	}

	public static Drive driveService() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

		return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

	}

	// Build a new authorized API client service.
	public static void list() throws GeneralSecurityException, IOException {
		Drive service = driveService();

		// Print the names and IDs for up to 10 files.
		FileList result = service.files().list().setPageSize(10).setFields("nextPageToken, files(id, name)").execute();
		List<File> files = result.getFiles();
		if (files == null || files.isEmpty()) {
			System.out.println("No files found.");
		} else {
			System.out.println("Files:");
			for (File file : files) {
				System.out.printf("%s (%s)\n", file.getName(), file.getId());
			}
		}
	}

	public static void upload() throws IOException, GeneralSecurityException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		File fileMetadata = new File();
		fileMetadata.setName("text.txt");
		java.io.File filePath = new java.io.File("src/main/resources/text.txt");
		FileContent mediaContent = new FileContent("text/plain", filePath);
		File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute();

		System.out.println("File ID: " + file.getId() + file.getName());

	}

	public static String download(String id) throws IOException, GeneralSecurityException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)

				.build();
		String res = "";
		// Solo funciona con los archivos que se haya subido desde la aplicacion

		File file = driveService().files().get(id).setFields("size").execute();
		// executeMediaAndDownloadTo no funciona si el archivo esta vacio
		if (file.getSize() != 0) {
			OutputStream outputStream = new ByteArrayOutputStream();
			service.files().get(id).executeMediaAndDownloadTo(outputStream);
			res=res+outputStream;
		}
			
		// Muestra por pantalla el contenido del archivo
		
		return res;

	}

	public static List<File> folderList() throws GeneralSecurityException, IOException {
		Drive service = driveService();
		System.out.println("folderList()");
		// Print the names and IDs for up to 10 files.
		FileList result = service.files().list()

				.setQ("mimeType = 'application/vnd.google-apps.folder'").setFields("nextPageToken, files(id, name)")
				.setSpaces("drive").execute();
		List<File> files = result.getFiles();
		for (File f : files) {
			System.out.println(f.toString());
		}
		return files;

	}

	private static boolean hasTheSameName(String name) throws GeneralSecurityException, IOException {
		Boolean same = false;
		// Busca entre la lista de carpetas si hay alguna carpeta con el mismo nombre
		List<File> folders = new ArrayList<>(folderList());
		for (File f : folders) {
			if (f.getName().contains(name)) {
				same = true;
			}
		}
		return same;
	}

	private static String createFolder() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		String res = "";
		System.out.println("CreateFolder()");
		if (hasTheSameName("New directory")) {
			System.out.println("Existe una carpeta con el mismo nombre");
		} else {

			File fileMetadata = new File();
			fileMetadata.setName("New directory");
			fileMetadata.setMimeType("application/vnd.google-apps.folder");
			fileMetadata.setFolderColorRgb("#b97123");
			File file = service.files().create(fileMetadata).setFields("id").execute();
			System.out.println("Folder ID: " + file.getId());
			res = file.getId();
		}
		return res;

	}

	private static File insertFileInFolder() throws GeneralSecurityException, IOException {
		Drive service = driveService();

		String folderId = getRepoFolder(userAuthenticated()).getId();
		File fileMetadata = new File();
		fileMetadata.setName("agua1.png");
		fileMetadata.setParents(Collections.singletonList(folderId));
		java.io.File filePath = new java.io.File("src/main/resources/Agua1.png");
		FileContent mediaContent = new FileContent("image/png", filePath);
		File file = service.files().create(fileMetadata, mediaContent).setFields("id, parents").execute();
		System.out.println("File ID: " + file.getId());
		return file;
	}

	private static File getFileByName(String name) throws GeneralSecurityException, IOException {
		File res;
		Drive service = driveService();
		String pageToken = null;
		do {
			FileList result = service.files().list().setQ("name='" + name + "'")
					.setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			res = result.getFiles().get(0);
			for (File file : result.getFiles()) {
				System.out.printf("Found file: %s (%s)\n", file.getName(), file.getId());
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		System.out.println(res.getName() + res.getId());
		return res;
	}

	public static List<String> getFilesByFolderId(String id) throws IOException, GeneralSecurityException {
		List<String> res = new ArrayList<>();
		String pageToken = null;
		do {
			FileList result = DriveQuickstart.driveService().files().list().setQ("'" + id + "' in parents")
					.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			for (File f : result.getFiles()) {
				res.add(f.getName());
				System.out.println(f.getName());

			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		return res;
	}

	private static void getFoldersByFolderId(String id) throws IOException, GeneralSecurityException {
		String pageToken = null;
		do {
			FileList result = DriveQuickstart.driveService().files().list()
					// Busca todos los archivos que no sean carpetas
					.setQ("'" + id + "' in parents and mimeType = 'application/vnd.google-apps.folder'")
					.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			for (File f : result.getFiles()) {
				System.out.println(f.getName());
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
	}

	private static File createFolderInFolder() throws GeneralSecurityException, IOException {
		Drive service = driveService();
		System.out.println("CreateFolderInFolder()");
		String folderId = createFolder();
		File fileMetadata = new File();
		fileMetadata.setName("New directory");
		fileMetadata.setFolderColorRgb("#99FF16");
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		fileMetadata.setParents(Collections.singletonList(folderId));
		File file = service.files().create(fileMetadata).setFields("id").execute();
		System.out.println("Folder ID: " + file.getId());
		return file;
	}

	private static void moveFilesBetweenFolders() throws GeneralSecurityException, IOException {
		Drive service = driveService();
		// Se le pasa la id del fichero que devuelve insertFileInFolder
		String fileId = insertFileInFolder().getId();
		String folderId = createFolderInFolder().getId();
		// Retrieve the existing parents to remove
		File file = service.files().get(fileId).setFields("parents").execute();

		StringBuilder previousParents = new StringBuilder();

		for (String parent : file.getParents()) {
			previousParents.append(parent);
			previousParents.append(',');
		}
		// Move the file to the new folder
		file = service.files().update(fileId, null).setAddParents(folderId).setRemoveParents(previousParents.toString())
				.setFields("id, parents").execute();
	}

	private static void getFoldersByRootDirectory() throws IOException, GeneralSecurityException {
		String pageToken = null;
		System.out.println("getFoldersByRootDirectory(");
		do {
			FileList result = DriveQuickstart.driveService().files().list()
					// Busca todos los archivos que no sean carpetas
					.setQ("'root' in parents and mimeType = 'application/vnd.google-apps.folder'").setSpaces("drive")
					.setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			for (File f : result.getFiles()) {
				System.out.println(f.getName());
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);

	}

	private static void delete(String id) throws GeneralSecurityException, IOException {
		Drive service = driveService();
		service.files().delete(id).execute();

	}

}