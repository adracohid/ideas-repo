package es.us.isa.ideas.repo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.api.services.drive.Drive;

import es.us.isa.ideas.repo.exception.AuthenticationException;
import es.us.isa.ideas.repo.exception.BadUriException;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.gdrive.DriveQuickstart;
import es.us.isa.ideas.repo.gdrive.GDriveDirectory;
import es.us.isa.ideas.repo.gdrive.GDriveFile;
import es.us.isa.ideas.repo.gdrive.GDriveProject;
import es.us.isa.ideas.repo.gdrive.GDriveRepo;
import es.us.isa.ideas.repo.gdrive.GDriveWorkspace;
import es.us.isa.ideas.repo.impl.fs.FSDirectory;
import es.us.isa.ideas.repo.impl.fs.FSFile;
import es.us.isa.ideas.repo.impl.fs.FSNode;
import es.us.isa.ideas.repo.impl.fs.FSProject;
import es.us.isa.ideas.repo.impl.fs.FSRepo;
import es.us.isa.ideas.repo.impl.fs.FSWorkspace;
import es.us.isa.ideas.repo.operation.Listable;

import static org.apache.commons.io.FilenameUtils.getExtension;

public class Othertest {
	private static String user;
	private static Drive credentials;

	private static String userAuthenticated() {
		DummyAuthenticationManagerDelegate authDelegate = new DummyAuthenticationManagerDelegate("yo");
		IdeasRepo.setAuthManagerDelegate(authDelegate);
		return authDelegate.getAuthenticatedUserId();
	}

	private static void deleteFSWorkspaces(String owner) {
		FSRepo r = new FSRepo();
		java.io.File f = new java.io.File(r.getRepoUri() + owner);
		String[] wsList = f.list();
		if (wsList.length != 0) {
			for (int i = 0; i < wsList.length; i++) {
				try {
					Facade.deleteWorkspace(wsList[i], user);
				} catch (AuthenticationException | BadUriException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private static void deleteGDriveWorkspace(String owner) {
		com.google.api.services.drive.model.File repoFolder;
		GDriveRepo gdr = new GDriveRepo(credentials);
		try {
			repoFolder = DriveQuickstart.getRepoFolder(owner, credentials);

			List<com.google.api.services.drive.model.File> folders = new ArrayList<>(
					DriveQuickstart.getFoldersByFolderId(repoFolder.getId(), credentials));
			for (com.google.api.services.drive.model.File f : folders) {
				Facade.deleteGDriveWorkspace(f.getName(), user, credentials);
			}
		} catch (IOException | GeneralSecurityException | AuthenticationException | BadUriException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		user = userAuthenticated();
		credentials = DriveQuickstart.driveService();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		deleteFSWorkspaces(user);
		deleteGDriveWorkspace(user);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSaveGDriveFile() throws AuthenticationException {

		System.out.println("Test save file ===================================================");
		// 1º Se crea el workspace

		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto

		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		
		// 3º Se crea el fichero

		// 4º Se sube el fichero
		GDriveFile file = new GDriveFile("datos.csv", workspace.getName(), project.getName(), user, credentials);
		boolean save = file.persist();
	
		System.out.println("Lista de nodos de gdriveWorkspace" + Facade.getGDriveWorkspaceTree(workspace.getName(), user, credentials));
		//System.out.println("extension del fichero: " + getExtension(file.getName()));
		//assertTrue(save);
		// Comprobar que el fichero existe
		//assertTrue(file.exist());

	}

	@Test
	public void testSaveFile() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("Test save file ============================================");
		FSWorkspace workspace2 = new FSWorkspace("workspace2", user);
		workspace2.persist();
		
		FSProject p1 = new FSProject("File project", workspace2.getName(), userAuthenticated());
		p1.persist();
		FSFile file1 = new FSFile("file1.jpg", workspace2.getName(), p1.getName(), userAuthenticated());
		file1.persist();
		// Comprobar que existe
		File ws = new File(IdeasRepo.get().getObjectFullUri(file1));
		assertTrue(ws.exists());
		System.out.println("lista de nodos FSWorkspace: " + workspace2.list());
		// Comprobar que file1.jpg existe en el workspace2
		assertTrue(workspace2.list().getChildren().toString().contains(file1.getName()));
	
		System.out.println("Lista workspace local: "+Facade.getWorkspaceTree(workspace2.getName(), user));
	}

	@Test
	public void uploadWorkspace() throws AuthenticationException {
		// 1º Creamos un workspace en local
		FSWorkspace wlocal = new FSWorkspace("wlocal", user);
		wlocal.persist();
		// 1.1 creamos un proyecto
		FSProject plocal = new FSProject("plocal", "wlocal", user);
		plocal.persist();
		// 1.2 creamos un fichero
		FSFile pfile = new FSFile("pfile.txt", "wlocal", "plocal", user);
		pfile.persist();

		// 2º Subimos el workspace
		boolean upload = wlocal.uploadWorkspaceToGdrive(credentials);
		System.out.println(upload);
	}

	@Test
	public void uploadFile() throws AuthenticationException {
		// 1º Creamos un workspace en local
		FSWorkspace wlocal = new FSWorkspace("wlocal", user);
		wlocal.persist();
		// 1.1 creamos un proyecto
		FSProject plocal = new FSProject("plocal", "wlocal", user);
		plocal.persist();
		// 1.2 creamos un fichero
		FSFile pfile = new FSFile("pfile.txt", "wlocal", "plocal", user);
		pfile.persist();

		// 2º Creamos el workspace en google drive
		GDriveWorkspace wgdrive = new GDriveWorkspace("wgdrive", user, credentials);
		wgdrive.persist();

		// 2.1 Creamos el/los proyecto/s

	}

	@Test
	public void testPath() throws AuthenticationException, ObjectClassNotValidException, IOException, GeneralSecurityException, BadUriException {
		// 1º Creamos un workspace en local
		FSWorkspace wlocal = new FSWorkspace("wlocal2", user);
		wlocal.persist();
		// 1.1 creamos un proyecto
		FSProject plocal = new FSProject("plocal", wlocal.getName(), user);
		plocal.persist();

		// 1.2 creamos un fichero
		FSFile pfile = new FSFile("pfile.txt", wlocal.getName(), "plocal", user);
		pfile.persist();
		// 1.3 Creamos un directory
		FSDirectory dlocal = new FSDirectory("dlocal", wlocal.getName(), plocal.getName(), user);
		dlocal.persist();

		// 1.4 Creamos otro fichero
		FSFile file2 = new FSFile("file2", wlocal.getName(), plocal.getName(), user);
		file2.persist();
		// 1.5 Movemos ese fichero al directory dlocal
		file2.move(dlocal, false);
		for (Node projects : wlocal.list().getChildren()) {
			for (Node f : projects.getChildren()) {
				System.out.println(f.getTitle());

			}
		}
		System.out.println("tree:"+Facade.getWorkspaceTree(wlocal.getName(),user));

	}
	
	@Test
	public void testDownload() throws IOException, GeneralSecurityException, AuthenticationException,
			ObjectClassNotValidException, BadUriException {
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		GDriveProject project2 = new GDriveProject("project2", workspace.getName(), user, credentials);
		project2.persist();
		GDriveDirectory directory = new GDriveDirectory("directoryp1", workspace.getName(), project.getName(), user,
				credentials);
		directory.persist();

		GDriveFile file = new GDriveFile("datos.txt", "workspace4", "project1", user, credentials);
		file.persist();
		file.write("Hello world!");
		GDriveFile filep1 = new GDriveFile("datosp1.txt", workspace.getName(), project.getName(), user, credentials);
		filep1.persist();
		GDriveFile filep2 = new GDriveFile("datosp2.txt", workspace.getName(), project2.getName(), user, credentials);
		filep2.persist();

		file.move(directory, false);
		
		boolean download=workspace.downloadWorkspace();
		assertTrue(download);
		//Comprobamos que existe en local
		FSWorkspace w=new FSWorkspace(workspace.getName(),user);
		
		java.io.File ws = new java.io.File(IdeasRepo.get().getObjectFullUri(w));

		assertTrue(ws.exists());

	}

	@Test
	public void testUpload() throws AuthenticationException, BadUriException, IOException, GeneralSecurityException,
			ObjectClassNotValidException {
		Facade.createWorkspace("wfile", user);

		Facade.createProject("wfile/proyect1", user);
		Facade.createProject("wfile/proyect2", user);
		Facade.createDirectory("wfile/proyect1/directp1", user);
		FSFile fdirectory = new FSFile("directp1/datos2.txt", "wfile", "proyect1", user);
		fdirectory.persist();

		Facade.createFile("wfile/proyect1/documento.csv", user);
		Facade.setFileContent("wfile/proyect1/documento.csv", user, "Hola, mundo");
		Facade.createFile("wfile/proyect1/datos1.csv", user);
		// Facade.createFile("wfile/proyect1/directp1/datos2.txt", user);

		fdirectory.write("dato1, dato2");

		FSWorkspace wlocal = new FSWorkspace("wfile", user);
		// DriveQuickstart.uploadWorkspace(wlocal.getName(), user,credentials);
		boolean upload=wlocal.uploadWorkspaceToGdrive(credentials);
		assertTrue(upload);
		//Comprobamos que existe el workspace en google drive
		GDriveWorkspace gw=new GDriveWorkspace(wlocal.getName(), user, credentials);
		assertTrue(gw.exist());
	}
	@Test
	public void testWorkspaceRepe() throws AuthenticationException {
		GDriveWorkspace workspaceOriginal=new GDriveWorkspace("wOriginal", user, credentials);
		workspaceOriginal.persist();
		workspaceOriginal.persist();
	}
	
	@Test
	public void testMoveDirectoryToDirectory() throws AuthenticationException, BadUriException {
		Facade.createGDriveWorkspace("WD", user, credentials);
		Facade.createGDriveProject("WD/project1", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directoryDest", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directoryOrigin", user, credentials);
		Facade.moveGDriveDirectory("WD/project1/directoryOrigin", user, "WD/project1/directoryDest", false, credentials);

	}
	
	@Test
	public void testMoveDirectoryToParentDirectory() throws BadUriException, AuthenticationException {
		Facade.createGDriveWorkspace("WD", user, credentials);
		Facade.createGDriveProject("WD/project1", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directory1", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directory1/IntoDirectory", user, credentials);
		boolean move=Facade.moveGDriveDirectory("WD/project1/directory1/IntoDirectory", user, "WD/project1/directory1", false, credentials);
		assertTrue(move);
	}
	
	@Test
	public void testMoveDirectoryToSameProject() throws BadUriException, AuthenticationException {
		Facade.createGDriveWorkspace("WorkspaceD", user, credentials);
		Facade.createGDriveProject("WorkspaceD/projecto2", user, credentials);
		Facade.createGDriveDirectory("WorkspaceD/projecto2/datos", user, credentials);
		Facade.createGDriveDirectory("WorkspaceD/projecto2/datos/clima", user, credentials);
		boolean move= Facade.moveGDriveDirectory("WorkspaceD/projecto2/datos/clima", user, "WorkspaceD/projecto2", true, credentials);
		assertTrue(move);
	}
	@Test
	public void testCreateDirectoryInDirectory() throws BadUriException, AuthenticationException {
		Facade.createGDriveWorkspace("WD", user, credentials);
		Facade.createGDriveProject("WD/project1", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directoryDest", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directoryDest/IntoDirectory", user, credentials);
	
	}
	@Test
	public void testCreateFileInDirectory() throws AuthenticationException, BadUriException {
		Facade.createGDriveWorkspace("WD", user, credentials);
		Facade.createGDriveProject("WD/project1", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directory2", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directory2/IntoDirectory", user, credentials);
		Facade.createGDriveFile("WD/project1/directory2/IntoDirectory/greetings.txt", user, credentials);
	}

}

