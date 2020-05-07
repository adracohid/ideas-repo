package es.us.isa.ideas.repo;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import es.us.isa.ideas.repo.exception.AuthenticationException;
import es.us.isa.ideas.repo.exception.BadUriException;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.gdrive.DriveQuickstart;
import es.us.isa.ideas.repo.gdrive.GDriveProject;
import es.us.isa.ideas.repo.gdrive.GDriveRepo;
import es.us.isa.ideas.repo.gdrive.GDriveWorkspace;
import es.us.isa.ideas.repo.impl.fs.FSFile;
import es.us.isa.ideas.repo.impl.fs.FSRepo;
import es.us.isa.ideas.repo.impl.fs.FSWorkspace;

public class FacadeTest {
	private static String user;
	private static Drive credentials;

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
		File repoFolder;
		GDriveRepo gdr = new GDriveRepo(credentials);
		try {
			repoFolder = DriveQuickstart.getRepoFolder(owner, credentials);

			List<File> folders = new ArrayList<>(DriveQuickstart.getFoldersByFolderId(repoFolder.getId(),credentials));
			for (File f : folders) {
				Facade.deleteGDriveWorkspace(f.getName(), user, credentials);
			}
		} catch (IOException | GeneralSecurityException | AuthenticationException | BadUriException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		user = DriveQuickstart.userAuthenticated();
		credentials=DriveQuickstart.driveService();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//una vez terminados los tests borramos todos los workspaces y el fichero .history
		deleteFSWorkspaces(user);
		deleteGDriveWorkspace(user);
		FSFile history = (FSFile) Facade.getFileFromUri("//.history",user);
		history.delete();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	// Create workspace
	@Test
	public void testCreateFSWorkspace() throws AuthenticationException {
		boolean res = Facade.createWorkspace("Workspace1", user);
		assertTrue(res);
	}

	@Test
	public void testCreateGDriveWorkspace() throws AuthenticationException, BadUriException {
		boolean res = Facade.createGDriveWorkspace("Workspace de Google drive", user, credentials);
		
		assertTrue(res);
	}
	

	@Test
	public void testgetFSWorkspaces() throws AuthenticationException {
		String s = Facade.getWorkspaces(user);
		System.out.println("FS: " + s);
	}

	@Test
	public void testgetGDriveWorkspaces() throws AuthenticationException {
		String s = Facade.getGDriveWorkspaces(user, credentials);
		System.out.println("gdrive: " + s);
	}
	@Test
	public void testGetAllWorkspaces() throws AuthenticationException {
		Facade.createWorkspace("Work", user);
		Facade.createWorkspace("Workout", user);
		Facade.createGDriveWorkspace("GWork", user, credentials);
		String s1=Facade.getWorkspaces(user);
		String s2=Facade.getGDriveWorkspaces(user, credentials);
		String all=s1+s2;
		System.out.println(all.replace("][", ", "));
	}
	//Save selected workspace
	@Test
	public void testSaveSelectedWorkspace() throws AuthenticationException, IOException, ObjectClassNotValidException, BadUriException {
		boolean res = Facade.createWorkspace("Workspace2", user);
		assertTrue(res);
		Facade.saveSelectedWorkspace("Workspace2", user);
		FSFile history = (FSFile) Facade.getFileFromUri("//.history", user);
		System.out.println(history.readAsString());

		// Comprobar que se ha creado el fichero .history
		java.io.File ws = new java.io.File(IdeasRepo.get().getObjectFullUri(history));
		assertTrue(ws.exists());
	}
	@Test
	public void testSaveSelectedGDriveWorkspace() throws AuthenticationException, IOException, BadUriException {
		boolean res=Facade.createGDriveWorkspace("GWorkspace2", user, credentials);
		assertTrue(res);
		Facade.saveSelectedWorkspace("GWorkspace2", user);
		FSFile history = (FSFile) Facade.getFileFromUri("//.history", user);
		System.out.println(history.readAsString());
		
	}
	
	//Get selected workspace
	@Test
	public void testGetSelectedWorkspace() throws AuthenticationException, IOException {
		boolean res = Facade.createWorkspace("Workspace3", user);
		assertTrue(res);
		// Se ejecuta saveSelectedWorkspace para que no de error
		Facade.saveSelectedWorkspace("Workspace3", user);
		String ws=Facade.getSelectedWorkspace(user);
		System.out.println(ws);
		// Comprobar que el workspace seleccionado este en los workspaces del
		// repositorio
		FSRepo repo=new FSRepo();
		assertTrue(repo.getWorkspaces(user).contains(ws));
		
	}
	@Test
	public void testGetSelectedGDriveWorkspace() throws IOException, AuthenticationException {
		boolean res=Facade.createGDriveWorkspace("GWorkspace3", user, credentials);
		assertTrue(res);
		Facade.saveSelectedWorkspace("GWorkspace3", user);
		String gws=Facade.getSelectedWorkspace(user);
		System.out.println(gws);
		GDriveRepo grepo=new GDriveRepo(credentials);
		assertTrue(grepo.getWorkspaces(user, credentials).contains(gws));
	}
	
	//Guardar un GDriveWorkspace y un FSWorkspace en un fichero .history
	@Test
	public void testGetSelected() throws AuthenticationException, IOException, BadUriException {
		boolean res = Facade.createWorkspace("Workspace36", user);
		assertTrue(res);
		boolean res2=Facade.createGDriveWorkspace("GWorkspace36", user, credentials);
		assertTrue(res2);
		Facade.saveSelectedWorkspace("Workspace36", user);
		String ws=Facade.getSelectedWorkspace(user);
		//System.out.println(ws);
		Facade.saveSelectedWorkspace("GWorkspace36", user);
		FSFile history = (FSFile) Facade.getFileFromUri("//.history", user);
		System.out.println(history.readAsString());
	}

	// Create proyect
	@Test
	public void testCreateFSProject() throws BadUriException, AuthenticationException {
		Facade.createWorkspace("w", user);
		boolean res = Facade.createProject("w/proyecto 1", user);
		assertTrue(res);
	}

	@Test
	public void testCreateGDriveProject() throws AuthenticationException, BadUriException {
		Facade.createGDriveWorkspace("Wproject", user,credentials);
		boolean res = Facade.createGDriveProject("Wproject/first_proyect", user, credentials);
		assertTrue(res);
	}

	// Create file
	@Test
	public void testCreateFSFile() throws AuthenticationException, BadUriException {
		Facade.createWorkspace("wfile", user);
		Facade.createProject("wfile/proyect", user);
		boolean res = Facade.createFile("wfile/proyect/documento.txt", user);
		assertTrue(res);

	}

	@Test
	public void testCreateGDriveFile() throws AuthenticationException, BadUriException {
		// Para subir un fichero hace falta que este en el repositorio
		Facade.createWorkspace("gdw", user);
		Facade.createProject("gdw/proyect", user);
		Facade.createFile("gdw/proyect/file.txt", user);

		Facade.createGDriveWorkspace("gdw", user, credentials);
		Facade.createGDriveProject("gdw/proyect", user,credentials);
		boolean res = Facade.createGDriveFile("gdw/proyect/file.txt", user, credentials);
		assertTrue(res);
	}

	// Create directory
	@Test
	public void testCreateFSDirectory() throws BadUriException, AuthenticationException {
		Facade.createWorkspace("Wdir", user);
		Facade.createProject("Wdir/sproyect", user);
		boolean res = Facade.createDirectory("Wdir/sproyect/newDirectory", user);
		assertTrue(res);
	}

	@Test
	public void testCreateGDriveDirectory() throws BadUriException, AuthenticationException {
		Facade.createGDriveWorkspace("Wdir", user, credentials);
		Facade.createGDriveProject("Wdir/sproyect", user,credentials);
		Facade.createGDriveDirectory("Wdir/sproyect/newDirectory", user,credentials);
	}

	// Test read-write
	@Test
	public void testReadWriteFSFileAsString() throws BadUriException, AuthenticationException {
		// 1º Creamos el fichero txt
		Facade.createWorkspace("Data", user);
		Facade.createProject("Data/dataproyect", user);
		Facade.createFile("Data/dataproyect/greetings.txt", user);
		// 2º Escribimos en el fichero
		Facade.setFileContent("Data/dataproyect/greetings.txt", user, "Hello world!");

		// 3º Leemos del fichero
		String read = Facade.getFileContent("Data/dataproyect/greetings.txt", user);

		assertEquals("Hello world!", read);
	}

	@Test
	public void testReadWriteFSFileAsByte() throws BadUriException, AuthenticationException {
		// 1º Creamos el fichero txt
		Facade.createWorkspace("Data", user);
		Facade.createProject("Data/dataproyect", user);
		Facade.createFile("Data/dataproyect/greetingsasbyte.txt", user);

		// 2º Escribimos en el fichero
		Facade.setFileContent("Data/dataproyect/greetingsasbyte.txt", user, "Hello world!".getBytes());

		// 3º Leemos del fichero
		byte[] read = Facade.getFileContentAsBytes("Data/dataproyect/greetingsasbyte.txt", user);

		assertArrayEquals("Hello world!".getBytes(), read);

	}

	@Test
	public void testReadWriteGDriveFileAsString() throws AuthenticationException, BadUriException {
		// 1º Creamos el archivo en el repositorio
		Facade.createWorkspace("Data2", user);
		Facade.createProject("Data2/dataproyect", user);
		Facade.createFile("Data2/dataproyect/greetings.txt", user);

		// 2º Lo subimos a Google Drive
		Facade.createGDriveWorkspace("Data2", user,credentials);
		Facade.createGDriveProject("Data2/dataproyect", user,credentials);
		Facade.createGDriveFile("Data2/dataproyect/greetings.txt", user,credentials);

		// 3º Escribimos en el fichero
		Facade.setGDriveFileContent("Data2/dataproyect/greetings.txt", user, "Hello world!",credentials);

		// 4º Leemos del fichero
		String read = Facade.getGDriveFileContent("Data2/dataproyect/greetings.txt", user,credentials);

		assertEquals("Hello world!", read);
	}

	@Test
	public void testReadWriteGDriveFileAsByte() throws BadUriException, AuthenticationException {
		// 1º Creamos el archivo en el repositorio
		Facade.createWorkspace("Data2", user);
		Facade.createProject("Data2/dataproyect", user);
		Facade.createFile("Data2/dataproyect/data.txt", user);

		// 2º Lo subimos a Google Drive
		Facade.createGDriveWorkspace("Data2", user,credentials);
		Facade.createGDriveProject("Data2/dataproyect", user,credentials);
		Facade.createGDriveFile("Data2/dataproyect/data.txt", user,credentials);

		// 3º Escribimos en el fichero
		Facade.setGDriveFileContent("Data2/dataproyect/data.txt", user, "Data".getBytes(),credentials);

		// 4º Leemos del fichero
		byte[] read = Facade.getGDriveFileContentAsBytes("Data2/dataproyect/data.txt", user,credentials);
		assertArrayEquals("Data".getBytes(), read);
	}
	//Get tree
	@Test
	public void testWorkspaceTree() throws BadUriException, AuthenticationException {
		Facade.createWorkspace("Data2", user);
		Facade.createProject("Data2/dataproyect", user);
		Facade.createFile("Data2/dataproyect/data.txt", user);
		String projectTree = Facade.getProjectTree("Data2", user, "dataproyect");
		String tree = Facade.getWorkspaceTree("Data2", user);
		System.out.println("Workspace tree: " + tree);
		System.out.println("Project tree: " + projectTree);
		assertNotEquals(tree, "");
		assertNotEquals(projectTree, "");
	}

	@Test
	public void testGDriveWorkspaceTree() throws BadUriException, AuthenticationException {
		Facade.createGDriveWorkspace("Data2", user,credentials);
		Facade.createGDriveProject("Data2/dataproyect", user,credentials);
		Facade.createGDriveDirectory("Data2/dataproyect/D", user,credentials);
		String tree = Facade.getGDriveWorkspaceTree("Data2", user,credentials);
		String projectTree = Facade.getGDriveProjectTree("Data2", user, "dataproyect",credentials);
		System.out.println("Workspace tree: " + tree);
		System.out.println("Project tree: " + projectTree);
		assertNotEquals(tree, "");
		assertNotEquals(projectTree, "");
	}
	//Delete project
	@Test
	public void testDeleteProject() throws BadUriException, AuthenticationException {
		Facade.createWorkspace("Data2", user);
		Facade.createProject("Data2/deleteproject", user);
		boolean res = Facade.deleteProject("Data2/deleteproject", user);
		assertTrue(res);
	}

	@Test
	public void testDeleteGDriveProject() throws BadUriException, AuthenticationException {
		Facade.createGDriveWorkspace("Data2", user,credentials);
		Facade.createGDriveProject("Data2/deleteProject", user,credentials);
		boolean res = Facade.deleteGDriveProject("Data2/deleteProject", user,credentials);
		assertTrue(res);
	}
	//Delete file
	@Test
	public void testDeleteFile() throws BadUriException, AuthenticationException {
		Facade.createWorkspace("Data2", user);
		Facade.createProject("Data2/dataproyect", user);
		Facade.createFile("Data2/dataproyect/delete.txt", user);
		boolean res = Facade.deleteFile("Data2/dataproyect/delete.txt", user);
		assertTrue(res);
	}

	@Test
	public void testDeleteGDriveFile() throws BadUriException, AuthenticationException {
		// Para subir un fichero hace falta que este en el repositorio
		Facade.createWorkspace("gdw", user);
		Facade.createProject("gdw/proyect2", user);
		Facade.createFile("gdw/proyect2/deletefile.txt", user);

		Facade.createGDriveWorkspace("gdw", user,credentials);
		Facade.createGDriveProject("gdw/proyect2", user,credentials);
		Facade.createGDriveFile("gdw/proyect2/deletefile.txt", user,credentials);
		boolean res = Facade.deleteGDriveFile("gdw/proyect2/deletefile.txt", user,credentials);
		assertTrue(res);
	}
	//Delete directory
	@Test
	public void testDeleteDirectory() throws BadUriException, AuthenticationException {
		Facade.createWorkspace("Data2", user);
		Facade.createProject("Data2/dataproyect", user);
		Facade.createDirectory("Data2/dataproyect/deletedirectory", user);
		boolean res = Facade.deleteDirectory("Data2/dataproyect/deletedirectory", user);
		assertTrue(res);
	}

	@Test
	public void testDeleteGDriveDirectory() throws BadUriException, AuthenticationException {
		Facade.createGDriveWorkspace("Data2", user,credentials);
		Facade.createGDriveProject("Data2/dataproyect", user,credentials);
		Facade.createGDriveDirectory("Data2/dataproyect/deletedirectory", user,credentials);
		boolean res = Facade.deleteGDriveDirectory("Data2/dataproyect/deletedirectory", user,credentials);
		assertTrue(res);
	}
	//Move directory
	@Test
	public void testMoveDirectory() throws BadUriException, AuthenticationException {
		Facade.createWorkspace("Data2", user);
		Facade.createProject("Data2/dataproyect", user);
		Facade.createDirectory("Data2/dataproyect/directory1", user);
		Facade.createProject("Data2/project2", user);
		Facade.createDirectory("Data2/dataproyect/directory2", user);
		boolean res = Facade.moveDirectory("Data2/dataproyect/directory1", user, "Data2/project2", true);
		System.out.println(Facade.getWorkspaceTree("Data2", user));
		assertTrue(res);

	}
	
	@Test
	public void testMoveGDriveDirectory() throws AuthenticationException, BadUriException {
		Facade.createGDriveWorkspace("Data2", user,credentials);
		Facade.createGDriveProject("Data2/dataproyect", user,credentials);
		Facade.createGDriveDirectory("Data2/dataproyect/directory1", user,credentials);
		Facade.createGDriveDirectory("Data2/dataproyect/directory2",user,credentials);
		boolean res=Facade.moveGDriveDirectory("Data2/dataproyect/directory1", user,"Data2/dataproyect/directory2", false,credentials);
		assertTrue(res);
	}	
	//Move file
	@Test
	public void testMoveFile() throws BadUriException, AuthenticationException {
		Facade.createWorkspace("Data3", user);
		Facade.createProject("Data3/sgridman", user);
		Facade.createDirectory("Data3/sgridman/D-irectory", user);
		Facade.createFile("Data3/sgridman/F.txt", user);
		boolean res=Facade.moveFile("Data3/sgridman/F.txt", user, "Data3/sgridman/D-irectory", false);
		String s=Facade.getWorkspaceTree("Data3", user);
		System.out.println(s);
		assertTrue(res);
	}
	@Test
	public void testMoveGDriveFile() throws BadUriException, AuthenticationException {
		//1º Creamos el archivo
		Facade.createWorkspace("Data3", user);
		Facade.createProject("Data3/sgridman", user);
		Facade.createFile("Data3/sgridman/F-ile.txt", user);
		
		//2º Lo subimos a gdrive
		Facade.createGDriveWorkspace("Data3", user,credentials);
		Facade.createGDriveProject("Data3/sgridman", user,credentials);
		Facade.createGDriveFile("Data3/sgridman/F-ile.txt", user,credentials);
		Facade.createGDriveDirectory("Data3/sgridman/D-irectory", user,credentials);
		
		
		//3º Movemos el fichero del proyecto al directorio
		boolean res=Facade.moveGDriveFile("Data3/sgridman/F-ile.txt", user, "Data3/sgridman/D-irectory", true,credentials);
		assertTrue(res);
	}
	//Rename file
	@Test
	public void testRenameGDriveFile() throws BadUriException, AuthenticationException, IOException, GeneralSecurityException{
		//1º Creamos el archivo
		Facade.createWorkspace("Data3", user);
		Facade.createProject("Data3/sgridman", user);
		Facade.createFile("Data3/sgridman/F-ile.txt", user);
		//TODO
		
		//Crear fichero temporal 
		//Subir el fichero a gdrive
		//Borrar el fichero temporal
		
		//2º Lo subimos a gdrive
		Facade.createGDriveWorkspace("Data3", user,credentials);
		Facade.createGDriveProject("Data3/sgridman", user,credentials);
		Facade.createGDriveFile("Data3/sgridman/F-ile.txt", user,credentials);
		boolean res=Facade.renameGDriveFile("Data3/sgridman/F-ile.txt", user, "data.txt",credentials);
		assertTrue(res);
	}
	
	@Test
	public void testRenameFile() throws BadUriException, AuthenticationException {
		Facade.createWorkspace("Data3", user);
		Facade.createProject("Data3/sgridman", user);
		Facade.createFile("Data3/sgridman/F-ile.txt", user);
		boolean res=Facade.renameFile("Data3/sgridman/F-ile.txt", user, "data.txt");
		assertTrue(res);
	}
	//Rename directory
	@Test
	public void testRenameDirectory() throws AuthenticationException, BadUriException {
		Facade.createWorkspace("Data2", user);
		Facade.createProject("Data2/dataproyect", user);
		Facade.createDirectory("Data2/dataproyect/directory1", user);
		boolean res=Facade.renameDirectory("Data2/dataproyect/directory1", user, "directory30");
		assertTrue(res);
	}
	
	@Test
	public void testRenameGDriveDirectory() throws AuthenticationException, BadUriException, IOException, GeneralSecurityException{
		Facade.createGDriveWorkspace("Data2", user,credentials);
		Facade.createGDriveProject("Data2/dataproyect", user,credentials);
		Facade.createGDriveDirectory("Data2/dataproyect/directory1", user,credentials);
		boolean res=Facade.renameGDriveDirectory("Data2/dataproyect/directory1",user, "directory29",credentials);
		assertTrue(res);
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
		assertTrue(!move);
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
		boolean dind=Facade.createGDriveDirectory("WD/project1/directoryDest/IntoDirectory", user, credentials);
		assertTrue(dind);
	}
	@Test
	public void testCreateFileInDirectory() throws AuthenticationException, BadUriException {
		Facade.createGDriveWorkspace("WD", user, credentials);
		Facade.createGDriveProject("WD/project1", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directory2", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directory2/IntoDirectory", user, credentials);
		boolean create=Facade.createGDriveFile("WD/project1/directory2/IntoDirectory/greetings.txt", user, credentials);
		assertTrue(create);
	}
	@Test
	public void testDownloadWorkspace() throws AuthenticationException, BadUriException {
		Facade.createGDriveWorkspace("WD", user, credentials);
		Facade.createGDriveProject("WD/project1", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directory2", user, credentials);
		Facade.createGDriveDirectory("WD/project1/directory2/IntoDirectory", user, credentials);
		Facade.createGDriveFile("WD/project1/directory2/IntoDirectory/greetings.txt", user, credentials);
		boolean download=Facade.downloadGDriveWorkspace("WD", user, credentials);
		assertTrue(download);
	}
	
	@Test
	public void testUploadWorkspace() throws AuthenticationException, BadUriException {
		Facade.createWorkspace("WLocal", user);
		Facade.createProject("WLocal/PLocal", user);
		Facade.createDirectory("WLocal/PLocal/java", user);
		Facade.createDirectory("WLocal/PLocal/java/main", user);
		Facade.createDirectory("WLocal/PLocal/java/main/calculos", user);
		Facade.createFile("WLocal/PLocal/java/main/calculos/suma.java", user);
		boolean upload=Facade.uploadWorkspace("WLocal", user, credentials,true);
		assertTrue(upload);
	}
}
