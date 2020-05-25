package es.us.isa.ideas.repo.gdrive;

import static org.hamcrest.CoreMatchers.containsString;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
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
import es.us.isa.ideas.repo.Facade;
import es.us.isa.ideas.repo.IdeasRepo;
import es.us.isa.ideas.repo.Node;
import es.us.isa.ideas.repo.exception.AuthenticationException;
import es.us.isa.ideas.repo.exception.BadUriException;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.impl.fs.FSDirectory;
import es.us.isa.ideas.repo.impl.fs.FSFile;
import es.us.isa.ideas.repo.impl.fs.FSNode;
import es.us.isa.ideas.repo.impl.fs.FSProject;
import es.us.isa.ideas.repo.impl.fs.FSWorkspace;
import es.us.isa.ideas.repo.operation.Listable;

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

	public static Credential authorize(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						// .setAccessType("online").build();
						.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		try {
			Credential credential = flow.loadCredential("usuariodeprueba608");
			if (credential != null && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() == null
					|| credential.getExpiresInSeconds() > 60)) {
				return credential;
			}
			// open in browser
			String redirectUri = receiver.getRedirectUri();
			AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri);
			System.out.println(authorizationUrl);
			// receive authorization code and exchange it for an access token
			String code = receiver.waitForCode();
			TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
			// store credential and return it
			return flow.createAndStoreCredential(response, "usuariodeprueba608");
		} finally {
			receiver.stop();
		}
	}

	public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
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
						// .setAccessType("online").build();
						.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static boolean logout() {
		boolean res = false;
		java.io.File token = new java.io.File("src/main/resources/" + userAuthenticated() + "/StoredCredential");
		if (token.exists()) {
			token.delete();
			res = true;
		}
		return res;
	}

	public static void main(String[] args) throws GeneralSecurityException, IOException {
		// getCredentials(GoogleNetHttpTransport.newTrustedTransport());
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

		// writeFile(uploadFile("text.txt").getId(), "Sample text");

	}

	public static boolean renameFile(String id, String newName, Drive credentials)
			throws IOException, GeneralSecurityException {
		boolean res = false;
		File f = credentials.files().get(id).setFields("name").execute();
		if (f != null) {
			f.setName(newName);
			credentials.files().update(id, f).execute();
			res = true;
		}
		return res;
	}

	public static void writeFile(String id, String msg, Drive credentials)
			throws IOException, GeneralSecurityException {
		
		File f=credentials.files().get(id).setFields("mimeType").execute();
		String type=f.getMimeType();
//		String res = "";

//		 res=msg;
		// 1º Creamos el buffer de escritura
		ByteArrayInputStream bais = new ByteArrayInputStream(msg.getBytes());
		//El contenido que se añada al fichero debe ser del mismo tipo que el fichero
		InputStreamContent content = new InputStreamContent(type, bais);
		//2º Actualizamos el fichero
		
		credentials.files().update(id, new File(), content).execute();
		

	}

	public static void writeFileAsByte(String id, byte[] msg, Drive credentials)
			throws IOException, GeneralSecurityException {
		File f=credentials.files().get(id).setFields("mimeType").execute();
		String type=f.getMimeType();
		// 1º Creamos el buffer de escritura
		ByteArrayInputStream bais = new ByteArrayInputStream(msg);
		InputStreamContent content = new InputStreamContent(type, bais);
		
		//2º Actualizamos el fichero	
		credentials.files().update(id, new File(), content).execute();
		

	}

	// NO SE PUEDE COPIAR CARPETAS
	public static void copyFileToFolder(String fileId, String folderId, Drive credentials)
			throws IOException, GeneralSecurityException {

		// 1º Se hace una copia de ese fichero
		File copy = credentials.files().copy(fileId, new File()).execute();

		// 2º Se mueve esa copia a la carpeta destino
		moveFileToFolder(copy.getId(), folderId, credentials);

	}

	// Tambien sirve para mover carpetas
	public static void moveFileToFolder(String fileId, String folderId, Drive credentials)
			throws IOException, GeneralSecurityException {
		// 1º Obtiene la carpeta donde esta el fichero (parents)
		Get f = credentials.files().get(fileId);
		// Set fields es para OBTENER EL CAMPO QUE TU QUIERAS
		f.setFields("parents, id");
		File fi = f.execute();
		// System.out.println("File id: " + fi.getId());

		String parents = String.join(",", fi.getParents());
		// System.out.println("Parents: " + parents);

		// 2º Actualiza el fichero añadiendo como parent la carpeta destino y borrando
		// la carpeta origen
		Update update = credentials.files().update(fileId, new File());
		//update.setFields("id, parents");
		update.setFields("id,parents");
		update.setAddParents(folderId);
		update.setRemoveParents(parents);

		File file = update.execute();
		// System.out.println("Copy id: " + file.getId());
		

	}
	


	public static boolean uploadFile(es.us.isa.ideas.repo.File file,String idParentFolder, Drive credentials) throws IOException, GeneralSecurityException, ObjectClassNotValidException {
		
		File fileMetadata = new File();
		String type = null;
		String[] fileName=file.getName().split("/");
		File f=getFileByName(fileName[fileName.length-1], idParentFolder, credentials);
		if(f!=null) {
			LOGGER.log(Level.INFO, "File " + f.getName() + " already exist");
			return false;
		}
		// Si el archivo contiene otro formato Google Drive se encarga de
		// cambiar el tipo de fichero automaticamente
		if (file.getName().contains(".txt")) {
			type = "text/plain";
		}
		
		
		fileMetadata.setName(fileName[fileName.length-1]);
		//com.google.api.services.drive.model.File project = DriveQuickstart.getProjectByName(file.getProject(),
		//		file.getWorkspace(), file.getOwner(), credentials);
		fileMetadata.setParents(Collections.singletonList(idParentFolder));
		java.io.File filePath = new java.io.File(IdeasRepo.get().getObjectFullUri(file));
		FileContent mediaContent = new FileContent(type, filePath);
		File gfile = credentials.files().create(fileMetadata, mediaContent).setFields("id").execute();
		return true;

	}

	public static File getDirectoryByName(String name, String project, String workspace, String owner,
			Drive credentials) throws IOException, GeneralSecurityException {
		File res;
		String[] splitName = name.split("\\\\");
		File file=null;
		
		if(splitName.length>1) { 
			//Lee de derecha a izquierda todos los directorios recursivamente
			//directory1\directory2......\directoryn
			String directoryName=name.replaceFirst("\\\\[^\\\\]+?$", "");

		file=getDirectoryByName(directoryName, project, workspace, owner, credentials);
		}else {
			file=getProjectByName(project, workspace, owner, credentials);
		}
		String pageToken = null;
		//Busqueda recursiva para encontrar el directorio padre
		
		
		
		//File file = getProjectByName(project, workspace, owner, credentials);
		// Si no existe el proyecto no devuelve nada
		if (file == null) {
			res = null;
		} else {
			do {
				FileList result = credentials.files().list()
						// Busca el fichero dentro del proyecto
						.setQ("'" + file.getId()
								+ "' in parents and mimeType='application/vnd.google-apps.folder' and name='" + splitName[splitName.length-1]
								+ "'")
						.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken)
						.execute();
				// Si no encuentra el fichero devuelve directamente null
				try {
					res = result.getFiles().get(0);
				} catch (IndexOutOfBoundsException e) {
					// System.out.println("The directory was not found");
					res = null;
				}
				pageToken = result.getNextPageToken();
			} while (pageToken != null);
		}
		return res;
	}

	public static File getFileByName(String name, String idParentFolder,Drive credentials) throws IOException {
		File res;
		File file=null;
		String pageToken=null;
		File parentFolder=credentials.files().get(idParentFolder).setFields("name").execute();
		if(parentFolder==null) {
			res=null;
		}else {
			do {
				FileList result = credentials.files().list()
						// Busca el fichero dentro del proyecto
						.setQ("'" + idParentFolder
								+ "' in parents and mimeType!='application/vnd.google-apps.folder' and name='" + name
								+ "'")
						.setSpaces("drive").setFields("nextPageToken, files(id, name,parents)").setPageToken(pageToken)
						.execute();
				// Si no encuentra el fichero devuelve directamente null
				try {
					res = result.getFiles().get(0);
				} catch (IndexOutOfBoundsException e) {
					System.out.println("The file was not found");
					res = null; 
				}
				pageToken = result.getNextPageToken();
			} while (pageToken != null);
		}
		return res;
	}
	public static File getFileByName(String name,String directory, String project, String workspace, String owner, Drive credentials)
			throws IOException, GeneralSecurityException {
		File res;
		File file=null;
		
		String pageToken = null;
		if(project==null) {
		file=getWorkspaceByName(workspace, owner, credentials);
		}else if(directory==null) {
		file = getProjectByName(project, workspace, owner, credentials);
		}else {
		file=getDirectoryByName(directory, project, workspace, owner, credentials);
		}
		// Si no existe el proyecto no devuelve nada
		if (file == null) {
			res = null;
		} else {
			do {
				FileList result = credentials.files().list()
						// Busca el fichero dentro del proyecto
						.setQ("'" + file.getId()
								+ "' in parents and mimeType!='application/vnd.google-apps.folder' and name='" + name
								+ "'")
						.setSpaces("drive").setFields("nextPageToken, files(id, name,parents)").setPageToken(pageToken)
						.execute();
				// Si no encuentra el fichero devuelve directamente null
				try {
					res = result.getFiles().get(0);
				} catch (IndexOutOfBoundsException e) {
					System.out.println("The file was not found");
					res = null;
				}
				pageToken = result.getNextPageToken();
			} while (pageToken != null);
		}

		return res;
	}

	public static File getProjectByName(String name, String workspace, String owner, Drive credentials)
			throws IOException, GeneralSecurityException {
		File res;
		String pageToken = null;
		File file = getWorkspaceByName(workspace, owner, credentials);
		// Si no existe el workspace no devuelve nada
		if (file == null) {
			res = null;
		} else {
			do {
				FileList result = credentials.files().list()
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
					// System.out.println("The project was not found");
					res = null;
				}
				pageToken = result.getNextPageToken();
			} while (pageToken != null);
		}
		return res;
	}

	public static GDriveProject getGDriveProjectFromUri(String path, String owner, Drive credentials)
			throws BadUriException {
		GDriveProject res = null;
		String[] split = path.split("/");
		if (split.length < 2) {
			throw new BadUriException("Bad uri, it should contains at 1 separator.");
		} else {
			res = new GDriveProject(split[1], split[0], owner, credentials);
		}
		return res;
	}

	public static File getWorkspaceByName(String name, String owner, Drive credentials)
			throws IOException, GeneralSecurityException {
		File res;
		String pageToken = null;
		do {
			FileList result = credentials.files().list()
					// Busca la carpeta dentro del directorio raiz,
					// comprueba que sea una carpeta
					// y que tenga el mismo nombre que el usuario
					.setQ("'" + getRepoFolder(owner, credentials).getId()
							+ "' in parents and mimeType='application/vnd.google-apps.folder' and name='" + name + "'")
					.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			// Si no encuentra el workspace devuelve directamente null
			try {

				res = result.getFiles().get(0);
			} catch (IndexOutOfBoundsException e) {
				res = null;
				// System.out.println("The workspace was not found");
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		return res;

	}

	public static File getRepoFolder(String owner, Drive credentials) throws IOException, GeneralSecurityException {
		File res = new File();
		String pageToken = null;
		do {
			FileList result = credentials.files().list()
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
				System.out.println("The repository was not found");
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		return res;
	}

	private static void delete(String id, Drive credentials) throws GeneralSecurityException, IOException {
		Drive service = credentials;
		service.files().delete(id).execute();

	}

	public static void upload(Drive credentials) throws IOException, GeneralSecurityException {

		File fileMetadata = new File();
		fileMetadata.setName("text.txt");
		java.io.File filePath = new java.io.File("src/main/resources/text.txt");
		FileContent mediaContent = new FileContent("text/plain", filePath);
		File file = credentials.files().create(fileMetadata, mediaContent).setFields("id").execute();

		System.out.println("File ID: " + file.getId() + file.getName());

	}

	public static Drive driveService() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

		// return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
		// getCredentials(HTTP_TRANSPORT))
		// .setApplicationName(APPLICATION_NAME).build();
		return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize(HTTP_TRANSPORT))
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

	public static String download(String id, Drive credentials) throws IOException, GeneralSecurityException {
		GoogleNetHttpTransport.newTrustedTransport();
//		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//				.setApplicationName(APPLICATION_NAME)
//
//				.build();
		String res = "";
		// Solo funciona con los archivos que se haya subido desde la aplicacion

		File file = credentials.files().get(id).setFields("size").execute();
		// executeMediaAndDownloadTo no funciona si el archivo esta vacio
		if (file.getSize() != 0) {
			OutputStream outputStream = new ByteArrayOutputStream();
			credentials.files().get(id).executeMediaAndDownloadTo(outputStream);
			res = res + outputStream;
		}

		return res;

	}

	public static void downloadWorkspace(String workspaceName,String user, Drive credentials) throws AuthenticationException, IOException, GeneralSecurityException, BadUriException, ObjectClassNotValidException {
	FSWorkspace workspaceLocal=new FSWorkspace(workspaceName,user);
	workspaceLocal.persist();
	GDriveWorkspace gworkspace=new GDriveWorkspace(workspaceName, user, credentials);
	FSNode wNode=(FSNode) IdeasRepo.get().getRepo("GDRIVE").list(gworkspace);
	getGDriveTree(wNode,workspaceName,null,user,credentials);
	String workspaceId=getWorkspaceByName(workspaceName, user, credentials).getId();
	
	//Al final del todo se borra el workspace de Google Drive
	credentials.files().delete(workspaceId).execute();
	
	}
	private static void getGDriveTree(FSNode node, String workspaceName,String pName, String user, Drive credentials) throws AuthenticationException, IOException, GeneralSecurityException, BadUriException, ObjectClassNotValidException {
		for (int i = 0; i < node.getChildren().size(); i++) {
			FSNode child = (FSNode) node.getChildren().get(i);
			System.out.println(child.getTitle());
			
			if (child.isFolder()) {
				System.out.println(child.getIcon());
				if(child.getIcon().equals("project_icon")) {
					String projectName=child.getTitle();
					FSProject project=new FSProject(projectName,workspaceName,user);
					project.persist();
					getGDriveTree(child,workspaceName,projectName,user,credentials);
				}else {
					String directoryName="";
					String[] splitURI=child.getKeyPath().split("/");
					for(int j=1;j<splitURI.length;j++) {
						directoryName+=splitURI[j];
						if(j<splitURI.length-1) {
							directoryName+="\\";
						}
					}
					
					String projectName=pName;
					FSDirectory directory=new FSDirectory(directoryName,workspaceName,projectName,user);
					directory.persist();
					
					getGDriveTree(child,workspaceName,projectName,user,credentials);
				}
			
			}else {
				
				//Si el node es un proyecto 
				if(node.getIcon().equals("project_icon")) {
					//Se crea el fichero local
					String fileName=child.getTitle();
					String projectName=node.getTitle();
					FSFile localFile=new FSFile(fileName, workspaceName, projectName, user);
					localFile.persist();
					//Se descarga el contenido del fichero de google drive y se escribe
					//en el fichero local
					String fileId=getFileByName(fileName,null, projectName, workspaceName, user, credentials).getId();
					String content=download(fileId, credentials);
					localFile.write(content);
					
				//Si el node es un directorio ...
				}else {		
					//El nombre de un fichero que se guarda en un directorio tiene dos partes
					//la primera que es el nombre del directorio y la segunda el nombre propio 
					//del archivo. 	
					String directoryName="";
					String[] splitURI=node.getKeyPath().split("/");
					for(int j=1;j<splitURI.length;j++) {
						directoryName+=splitURI[j];
						if(j<splitURI.length-1) {
							directoryName+="\\";
						}
					}
					String projectName=splitURI[0];
					String fileName=directoryName+"/"+child.getTitle();
					
					//String fileName=node.getTitle()+"/"+child.getTitle();
					//1º Creamos el fichero local
					FSFile flocal=new FSFile(fileName,workspaceName,projectName,user);
					flocal.persist();
					
					//2º Descargamos el contenido del fichero de google drive
					//File file=getFileByName(child.getTitle(), node.getTitle(), projectName, workspaceName, user, credentials);
					File directory=getDirectoryByName(directoryName, projectName, workspaceName, user, credentials);
					File file=getFileByName(child.getTitle(),directory.getId(),credentials);
					String content=download(file.getId(), credentials);
					
					//3º Escribimos el contenido en el fichero local
					flocal.write(content);
					
					
					
				}
				
			}
			}
}

	public static void uploadWorkspace(String workspaceName, String user, Drive credentials, Boolean test)
			throws AuthenticationException, IOException, GeneralSecurityException, BadUriException, ObjectClassNotValidException {
		
		FSWorkspace ws = new FSWorkspace(workspaceName, user);

		//1º Creo un workspace con el mismo nombre en google drive
		GDriveWorkspace gdw=new GDriveWorkspace(workspaceName,user,credentials);
		gdw.persist();
		FSNode wsNode =(FSNode) IdeasRepo.get().getRepo().list(ws);
		System.out.println(wsNode.getChildren().size());
		
		//Hace una busqueda recursiva por las carpetas
		getTree(wsNode,workspaceName,null,user,credentials,test);
		//Y al final del todo se borra el workspace local

		ws.delete();
		}
	

	private static void getTree(FSNode node,String workspaceName,String project,String user,Drive credentials, Boolean test) throws AuthenticationException, IOException, GeneralSecurityException, BadUriException, ObjectClassNotValidException {
		
		for (int i = 0; i < node.getChildren().size(); i++) {
			FSNode child = (FSNode) node.getChildren().get(i);
			System.out.println(child.getTitle());
			if (child.isFolder()) {
				System.out.println(child.getIcon());
				if(child.getIcon().equals("project_icon")) {
					String projectName=child.getTitle();
					GDriveProject gproject=new GDriveProject(projectName,workspaceName,user,credentials);
					gproject.persist();
					getTree(child,workspaceName,projectName,user,credentials,test);
				}else {
					String directoryName="";
					String[] splitURI=child.getKeyPath().split("/");
					if(test) {
						for(int j=3;j<splitURI.length;j++) {
							directoryName+=splitURI[j];
							if(j<splitURI.length-1) {
								directoryName+="\\";
							}
						}
					}else {
						for(int j=1;j<splitURI.length;j++) {
							directoryName+=splitURI[j];
							if(j<splitURI.length-1) {
								directoryName+="\\";
							}
						}
					}
						
					
					String projectName=project;
					GDriveDirectory gdirectory=new GDriveDirectory(directoryName,workspaceName,projectName,user,credentials);
					gdirectory.persist();
					getTree(child,workspaceName,projectName,user,credentials,test);
				}
			
			}else {
				//Si el node es un proyecto guarda el fichero dentro de ese proyecto
				if(node.getIcon().equals("project_icon")) {
					FSFile flocal=new FSFile(child.getTitle(),workspaceName,node.getTitle(),user);
					String idParentFolder=getProjectByName(node.getTitle(), workspaceName, user, credentials).getId();
					uploadFile(flocal, idParentFolder, credentials);
					//es.us.isa.ideas.repo.File flocal=Facade.getFileFromUri(node.getKeyPath(), owner)
				
				//Si el node es un directorio ...
				}else {		
					//El nombre de un fichero que se guarda en un directorio tiene dos partes
					//la primera que es el nombre del directorio y la segunda el nombre propio 
					//del archivo. 
					String directoryName="";
					String[] splitURI=node.getKeyPath().split("/");
					
						
					if(test) {
						for(int j=3;j<splitURI.length;j++) {
							directoryName+=splitURI[j];
							if(j<splitURI.length-1) {
								directoryName+="\\";
							}
						}	
					}else {
					for(int j=1;j<splitURI.length;j++) {
						directoryName+=splitURI[j];
						if(j<splitURI.length-1) {
							directoryName+="\\";
						}
					}
					}
					
					String[] splitdirectory=directoryName.split("\\\\");
					String projectName=splitURI[splitURI.length-1-splitdirectory.length];
					String fileName=directoryName+"/"+child.getTitle();
					FSFile flocal=new FSFile(fileName,workspaceName,projectName,user);
					String idParentFolder=getDirectoryByName(directoryName, flocal.getProject(), workspaceName, user, credentials).getId();									
					
					uploadFile(flocal, idParentFolder, credentials);
				}
				
			}
			}
	}

	public static List<File> folderList(Drive credentials) throws GeneralSecurityException, IOException {
		Drive service = credentials;
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

	public static List<File> getFilesByFolderId(String id, Drive credentials)
			throws IOException, GeneralSecurityException {
		List<File> res = new ArrayList<>();
		String pageToken = null;
		do {
			FileList result = credentials.files().list().setQ("'" + id + "' in parents").setSpaces("drive")
					.setFields("nextPageToken, files(id, name, mimeType,parents)").setPageToken(pageToken).execute();
			for (File f : result.getFiles()) {
				res.add(f);
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		return res;
	}

	public static List<File> getFoldersByFolderId(String id, Drive credentials)
			throws IOException, GeneralSecurityException {
		List<File> res = new ArrayList<>();
		String pageToken = null;
		do {
			FileList result = credentials.files().list()
					// Busca todos los archivos que no sean carpetas
					.setQ("'" + id + "' in parents and mimeType = 'application/vnd.google-apps.folder'")
					.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			for (File f : result.getFiles()) {
				res.add(f);
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		return res;
	}

	/*
	 * private static boolean hasTheSameName(String name,Drive credentials) throws
	 * GeneralSecurityException, IOException { Boolean same = false; // Busca entre
	 * la lista de carpetas si hay alguna carpeta con el mismo nombre List<File>
	 * folders = new ArrayList<>(folderList(credentials)); for (File f : folders) {
	 * if (f.getName().contains(name)) { same = true; } } return same; } private
	 * static String createFolder(Drive credentials) throws
	 * GeneralSecurityException, IOException { final NetHttpTransport HTTP_TRANSPORT
	 * = GoogleNetHttpTransport.newTrustedTransport(); Drive service = new
	 * Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
	 * .setApplicationName(APPLICATION_NAME).build(); String res = "";
	 * System.out.println("CreateFolder()"); if
	 * (hasTheSameName("New directory",credentials)) {
	 * System.out.println("Existe una carpeta con el mismo nombre"); } else {
	 * 
	 * File fileMetadata = new File(); fileMetadata.setName("New directory");
	 * fileMetadata.setMimeType("application/vnd.google-apps.folder");
	 * fileMetadata.setFolderColorRgb("#b97123"); File file =
	 * service.files().create(fileMetadata).setFields("id").execute();
	 * System.out.println("Folder ID: " + file.getId()); res = file.getId(); }
	 * return res;
	 * 
	 * } private static File createFolderInFolder(Drive credentials) throws
	 * GeneralSecurityException, IOException { Drive service = credentials;
	 * System.out.println("CreateFolderInFolder()"); String folderId =
	 * createFolder(credentials); File fileMetadata = new File();
	 * fileMetadata.setName("New directory");
	 * fileMetadata.setFolderColorRgb("#99FF16");
	 * fileMetadata.setMimeType("application/vnd.google-apps.folder");
	 * fileMetadata.setParents(Collections.singletonList(folderId)); File file =
	 * service.files().create(fileMetadata).setFields("id").execute();
	 * System.out.println("Folder ID: " + file.getId()); return file; }
	 * 
	 * private static File insertFileInFolder(Drive credentials) throws
	 * GeneralSecurityException, IOException {
	 * 
	 * 
	 * String folderId = getRepoFolder(userAuthenticated(), credentials).getId();
	 * File fileMetadata = new File(); fileMetadata.setName("agua1.png");
	 * fileMetadata.setParents(Collections.singletonList(folderId)); java.io.File
	 * filePath = new java.io.File("src/main/resources/Agua1.png"); FileContent
	 * mediaContent = new FileContent("image/png", filePath); File file =
	 * credentials.files().create(fileMetadata,
	 * mediaContent).setFields("id, parents").execute();
	 * System.out.println("File ID: " + file.getId()); return file; }
	 * 
	 * private static void deleteFolder(Drive credentials) throws IOException,
	 * GeneralSecurityException { Drive service = credentials; String pageToken =
	 * null; File f = new File(); do { FileList result =
	 * service.files().list().setQ("name contains 'New directory'")
	 * .setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute(
	 * ); f = result.getFiles().get(0); pageToken = result.getNextPageToken(); }
	 * while (pageToken != null); credentials.files().delete(f.getId()).execute();
	 * 
	 * }
	 * 
	 * private static void moveFilesBetweenFolders(Drive credentials) throws
	 * GeneralSecurityException, IOException { Drive service = credentials; // Se le
	 * pasa la id del fichero que devuelve insertFileInFolder String fileId =
	 * insertFileInFolder(credentials).getId(); String folderId =
	 * createFolderInFolder(credentials).getId(); // Retrieve the existing parents
	 * to remove File file =
	 * service.files().get(fileId).setFields("parents").execute();
	 * 
	 * StringBuilder previousParents = new StringBuilder();
	 * 
	 * for (String parent : file.getParents()) { previousParents.append(parent);
	 * previousParents.append(','); } // Move the file to the new folder file =
	 * service.files().update(fileId,
	 * null).setAddParents(folderId).setRemoveParents(previousParents.toString())
	 * .setFields("id, parents").execute(); }
	 * 
	 * private static void getFoldersByRootDirectory(Drive credentials) throws
	 * IOException, GeneralSecurityException { String pageToken = null;
	 * System.out.println("getFoldersByRootDirectory("); do { FileList result =
	 * credentials.files().list() // Busca todos los archivos que no sean carpetas
	 * .setQ("'root' in parents and mimeType = 'application/vnd.google-apps.folder'"
	 * ).setSpaces("drive")
	 * .setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute(
	 * ); for (File f : result.getFiles()) { System.out.println(f.getName()); }
	 * pageToken = result.getNextPageToken(); } while (pageToken != null);
	 * 
	 * }
	 * 
	 */
}