package es.us.isa.ideas.repo;

import static org.junit.Assert.*;

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
import es.us.isa.ideas.repo.gdrive.GDriveDirectory;
import es.us.isa.ideas.repo.gdrive.GDriveFile;
import es.us.isa.ideas.repo.gdrive.GDriveProject;
import es.us.isa.ideas.repo.gdrive.GDriveRepo;
import es.us.isa.ideas.repo.gdrive.GDriveWorkspace;
import es.us.isa.ideas.repo.impl.fs.FSFile;
import es.us.isa.ideas.repo.impl.fs.FSRepo;
import es.us.isa.ideas.repo.impl.fs.FSWorkspace;

public class GDriveTest {
	private static String user;
	private static Drive credentials;

	private static String userAuthenticated() {
		DummyAuthenticationManagerDelegate authDelegate = new DummyAuthenticationManagerDelegate("yo");
		IdeasRepo.setAuthManagerDelegate(authDelegate);
		return authDelegate.getAuthenticatedUserId();
	}

	// Crea una carpeta de repositorio antes de comenzar los test

	

	private static boolean deleteGDriveWorkspace(String owner) {
		boolean res=false;
		File repoFolder;
		GDriveRepo gdr = new GDriveRepo(credentials);
		try {
			repoFolder = DriveQuickstart.getRepoFolder(owner, credentials);

			List<File> folders = new ArrayList<>(DriveQuickstart.getFoldersByFolderId(repoFolder.getId(),credentials));
			for (File f : folders) {
				res=Facade.deleteGDriveWorkspace(f.getName(), user, credentials);
			}
		} catch (IOException | GeneralSecurityException | AuthenticationException | BadUriException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}


	@BeforeClass

	public static void setUpBeforeClass() throws Exception {
		user = userAuthenticated();
		credentials = DriveQuickstart.driveService();
		/*
		 * No hace falta File fileMetadata = new File();
		 * fileMetadata.setName(userAuthenticated());
		 * fileMetadata.setMimeType("application/vnd.google-apps.folder");
		 * DriveQuickstart.driveService().files().create(fileMetadata).setFields("id").
		 * execute();
		 * 
		 */

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// una vez terminados los tests borramos todos los workspaces y el fichero
		// .history
		boolean wgdrive=deleteGDriveWorkspace(user);
		assertTrue(wgdrive);
		// TODO
		FSFile history = (FSFile) Facade.getFileFromUri("//.history", user);
		history.delete();

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

//GDriveWorkspace#########################################################
	@Test
	public void testSaveGDWorkspace() throws AuthenticationException, IOException, GeneralSecurityException {
		GDriveWorkspace gdworkspace = new GDriveWorkspace("workspace1", userAuthenticated(),
				credentials);
		gdworkspace.persist();

		assertTrue(gdworkspace.exist());
	}

	@Test
	public void testSaveSameWorkspace() throws AuthenticationException, GeneralSecurityException, IOException {
		GDriveWorkspace gdworkspace = new GDriveWorkspace("Same_workspace", userAuthenticated(),
				credentials);
		gdworkspace.persist();
		GDriveWorkspace gdworkspace2 = new GDriveWorkspace("Same_workspace", userAuthenticated(),
				credentials);
		boolean res2 = gdworkspace2.persist();
		assertTrue(gdworkspace.exist());
		assertTrue(!res2);
	}

	@Test
	public void testRepoFolder() throws IOException, GeneralSecurityException {
		GDriveWorkspace gdworkspace = new GDriveWorkspace("workspace1", userAuthenticated(), credentials);
		System.out.println("Test repo folder ====================");
		System.out.println(DriveQuickstart.getRepoFolder(gdworkspace.getOwner(), credentials).toString());
		assertTrue(gdworkspace.exist());
	}

	@Test
	public void testDelete() {
		GDriveWorkspace gw = new GDriveWorkspace("workspace2", userAuthenticated(), credentials);
		System.out.println("Test delete========================");
		try {
			gw.persist();
			assertTrue(gw.exist());
			boolean t = gw.delete();
			assertTrue(t == true);
			assertTrue(!gw.exist());
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	// Intentar borrar un workspace que no existe
	@Test
	public void testDeleteFail() {
		System.out.println("Test delete fail==========================");
		GDriveWorkspace gw = new GDriveWorkspace("workspace3", user, credentials);
		try {
			boolean f = gw.delete();
			assertTrue(f == false);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

//GDriveProject#############################################################

	@Test
	public void testSaveProject() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("Test save project ===================================");
		// 1º Se crea el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		// Comprobar que existe
		assertTrue(project.exist());
		System.out.println(project.getName());

	}

	@Test
	public void testSaveSameProject() throws AuthenticationException {
		// 1º Se crea el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("SWorkspace", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("same_project", workspace.getName(), user, credentials);
		boolean res = project.persist();
		GDriveProject project2 = new GDriveProject("same_project", workspace.getName(), user, credentials);
		boolean res2 = project2.persist();
		assertTrue(res);
		assertFalse(res2);
	}

	// Intentar guardar un proyecto en un workspace que no existe
	@Test
	public void testSaveProjectFail() throws AuthenticationException {
		System.out.println("Test save project fail ==================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace56", user, credentials);
		assertTrue(!workspace.exist());
		GDriveProject project = new GDriveProject("project2", workspace.getName(), user, credentials);
		boolean save = project.persist();
		assertTrue(save == false);

	}

	@Test
	public void testListProject() throws AuthenticationException {
		System.out.println("Test list project ===========================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		GDriveProject project = new GDriveProject("project1", "workspace4", user, credentials);
		project.persist();
		System.out.println(project.list().toString());
	}

	@Test
	public void testDeleteProject() throws AuthenticationException {
		System.out.println("Test delete project ===================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		GDriveProject project = new GDriveProject("project1", "workspace4", user, credentials);
		project.persist();

		boolean delete = project.delete();
		assertTrue(delete == true);
		assertTrue(!project.exist());

	}

	// Intentar borrar un proyecto que no existe
	@Test
	public void testDeleteProjectFail() throws AuthenticationException {
		System.out.println("Test delete project fail =====================================");
		GDriveProject project = new GDriveProject("project3", "workspace6", user, credentials);

		boolean delete = project.delete();
		assertTrue(delete == false);
	}

	// Intentar borrar un proyecto que no existe
	@Test
	public void testDeleteProjectFail2() throws AuthenticationException {
		System.out.println("Test delete project fail 2 ======================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		GDriveProject project = new GDriveProject("project64", "workspace4", user, credentials);
		project.delete();
	}

	// Mover un proyecto a otro workspace
	@Test
	public void testMoveProject() throws AuthenticationException {
		System.out.println("Test move project ===================================");
		// 1º Se crea el workspace origen
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Se crea el workspace destino
		GDriveWorkspace dest = new GDriveWorkspace("workspace5", user, credentials);
		dest.persist();
		// 3º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		// 4º Mover el proyecto
		project.move(dest, false);

		// Comprobar que ahora esta en el workspace destino
		assertTrue(dest.list().toString().contains(project.getName()));

	}

	// Mover el proyecto a un workspace que tiene un proyecto con el mismo nombre
	@Test
	public void testMoveSameProject() throws AuthenticationException {
		System.out.println("Test move same project ===================================");
		// 1º Se crea el workspace origen
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("same_name", workspace.getName(), user, credentials);
		project.persist();

		// 3º Se crea el workspace destino
		GDriveWorkspace dest = new GDriveWorkspace("workspace5", user, credentials);
		dest.persist();
		// 4º Se crea un proyecto en el workspace destino
		GDriveProject projectt = new GDriveProject("same_name", dest.getName(), user, credentials);
		projectt.persist();
		// 5º Mover el proyecto
		boolean move=project.move(dest, false);
		assertTrue(!move);
	}

	// Intentar mover un proyecto a un workspace que no existe
	@Test
	public void testMoveProjectFail() throws AuthenticationException {
		System.out.println("Test move project fail ==========================================");
		GDriveWorkspace w = new GDriveWorkspace("Workspace4", user, credentials);
		GDriveProject project = new GDriveProject("project1", w.getName(), user, credentials);
		project.persist();

		boolean move = project.move(w, false);
		// Comprobar que no se ha podido realizar la operacion
		assertTrue(!move);
	}

	@Test
	public void testSaveFile2() throws AuthenticationException {
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		GDriveFile file = new GDriveFile("datos.txt", "workspace4", "project1", user, credentials);
		boolean save = file.persist();
		assertTrue(save);

	}

	// GDriveFile###############################################################################
	@Test
	public void testSaveFile() throws AuthenticationException {

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

		assertTrue(save);
		// Comprobar que el fichero existe
		assertTrue(file.exist());

	}

	@Test
	public void testTemporalyFile() throws AuthenticationException, IOException {
		// Crear workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace_temp", user, credentials);
		workspace.persist();

		// Crear proyecto
		GDriveProject project = new GDriveProject("project_temp", workspace.getName(), user, credentials);
		project.persist();

		// Crear fichero
		
		
		GDriveFile file = new GDriveFile("fichero_temporal.txt", workspace.getName(), project.getName(), user,
				credentials);
		boolean save = file.persist();
		assertTrue(save);
	

	}



	// Eliminar un fichero
	@Test
	public void testDeleteFile() throws AuthenticationException {
		System.out.println("Test delete file ===================================================");
		// 1º Se crea el workspace
		
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto

		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();

		// 3º Se sube el fichero
		GDriveFile file = new GDriveFile("datos4.csv", workspace.getName(), project.getName(), user, credentials);
		boolean save = file.persist();

		assertTrue(save);
		// Comprobar que el fichero existe
		assertTrue(file.exist());

		// Borramos el fichero
		file.delete();

		// Comprobamos que ya no existe
		assertTrue(!file.exist());
	}

	// Intentar eliminar un archivo que no existe
	@Test
	public void testDeleteFileFailed() throws AuthenticationException {

		System.out.println("Test delete file failed ===================================================");

		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto

		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();

		// 3º Se sube el fichero
		GDriveFile file = new GDriveFile("datos3.txt", workspace.getName(), project.getName(), user, credentials);

		// Comprobar que no existe
		assertTrue(!file.exist());
		boolean delete = file.delete();
		assertTrue(!delete);
	}

	// Escribir y leer un fichero
	@Test
	public void testWriteReadFile() throws AuthenticationException {
		System.out.println("Test write & read file ===================================================");
		// 1º Se crea el workspace

		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto

		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();

		// 3º Se crea el fichero

		// 4º Se sube el fichero
		GDriveFile file = new GDriveFile("datos4.txt", workspace.getName(), project.getName(), user, credentials);
		file.persist();
		// Escribir en el archivo
		boolean write = file.write("DATOS");
		assertTrue(write);
		GDriveFile newFile = new GDriveFile("datos4.txt", workspace.getName(), project.getName(), user, credentials);

		// leer el archivo
		assertEquals("DATOS", newFile.readAsString());
	}

	// Escribir y leer un fichero como un byte
	@Test
	public void testWriteReadFileAsByte() throws AuthenticationException {
		System.out.println("Test write & read file as byte ===================================================");
		// 1º Se crea el workspace

		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto

		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();

		// 3º Se crea el fichero


		// 4º Se sube el fichero
		GDriveFile file = new GDriveFile("datos5.csv", workspace.getName(), project.getName(), user, credentials);
		file.persist();
		// Escribir en el archivo
		boolean write = file.write("DATOS".getBytes());
		assertTrue(write);

		// leer el archivo
		assertArrayEquals("DATOS".getBytes(),file.readAsBytes());
	}

	// Escribir y leer en un fichero que no exista
	@Test
	public void testWriteReadFileFail() throws AuthenticationException {
		System.out.println("Test write & read file fail ==========================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		GDriveFile file = new GDriveFile("datos6.txt", workspace.getName(), project.getName(), user, credentials);
		assertTrue(!file.exist());
		boolean write = file.write("DATOS");
		assertTrue(!write);
		assertNull(file.readAsString());

	}

	// Escribir como bytes en un fichero que no exista
	@Test
	public void testWriteFileAsByteFail() throws AuthenticationException {
		System.out.println("Test write file as byte fail =========================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		GDriveFile file = new GDriveFile("datos6.txt", workspace.getName(), project.getName(), user, credentials);
		assertTrue(!file.exist());
		boolean write = file.write("DATOS".getBytes());
		assertTrue(!write);
	}

	// GDriveDirectory
	// ################################################################################

	// Guardar directory
	@Test
	public void testSaveDirectory() throws AuthenticationException {
		System.out.println("Test save directory ==================================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		// 3º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(), user,
				credentials);
		boolean save = directory.persist();
		assertTrue(directory.exist());
		assertTrue(save);
	}

	// Intentar guardar el directory en un proyecto que no exista
	@Test
	public void testSaveDirectoryFail() throws AuthenticationException {
		System.out.println("Test save directory fail ===========================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project14", workspace.getName(), user, credentials);
		
		// 3º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(), user,
				credentials);
		boolean save = directory.persist();
		assertTrue(!save);
		assertTrue(!directory.exist());
		
	}

	// Eliminar una carpeta
	@Test
	public void testDeleteDirectory() throws AuthenticationException {
		System.out.println("Test delete directory ==================================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		// 3º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(), user,
				credentials);
		directory.persist();
		boolean delete = directory.delete();
		assertTrue(!directory.exist());
		assertTrue(delete);
	}

	// Intentar eliminar una carpeta que no existe
	@Test
	public void testDeleteDirectoryFail() throws AuthenticationException {
		System.out.println("Test delete directory fail ==================================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		// 3º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(), user,
				credentials);
		boolean delete = directory.delete();
		assertTrue(!delete);
	}

	// Mover la carpeta de un proyecto a otro
	@Test
	public void testMoveDirectory() throws AuthenticationException {
		System.out.println("Test move directory ==================================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto origen
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		// 3º Crear el proyecto destino
		GDriveProject dest = new GDriveProject("Proyecto destino", workspace.getName(), user, credentials);
		dest.persist();
		// 4º Crear la carpeta
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(), user,
				credentials);
		directory.persist();
		// 5º Mover la carpeta al proyecto destino
		directory.move(dest, true);
		System.out.println(dest.list().getChildren());
		// Comprobamos que el directorio existe dentro del proyecto dest
		assertTrue(dest.list().toString().contains(directory.getName()));
	}
	
	// Intentar mover una carpeta a otra que no sea un directorio
	@Test
	public void testMoveDirectoryFail2() throws AuthenticationException {
		System.out.println("Test move directory fail 2==================================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto origen
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		// 3º Crear la carpeta
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(), user,
				credentials);
		directory.persist();
		// 4º Mover la carpeta al workspace
		boolean move = directory.move(workspace, true);
		// Comprobamos que no se ha podido hacer la operacion
		assertTrue(!move);

	}

	//Mover un directorio a otro directorio
	@Test
	public void testMoveDirectoryToDirectory() throws AuthenticationException {
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();
		//3º Crear el directorio origen
		GDriveDirectory dirOrigin=new GDriveDirectory("dirOrigin", workspace.getName(), project.getName(), user, credentials);
		dirOrigin.persist();
		//4º Crear el directorio destino
		GDriveDirectory dirDest1=new GDriveDirectory("dirDest1",workspace.getName(),project.getName(),user,credentials);
		dirDest1.persist();
		GDriveDirectory dirDest2=new GDriveDirectory("dirDest1\\dirDest2",workspace.getName(),project.getName(),user,credentials);
		dirDest2.persist();
		
		
		//Mover el directorio
		GDriveDirectory dest=new GDriveDirectory("dirDest1\\dirDest2\\dirOrigin", workspace.getName(), project.getName(), user, credentials);
		boolean move=dirOrigin.move(dirDest2, false);
		assertTrue(move);
	}
	
	//Mover un directorio al mismo directorio
	@Test
	public void testMoveDirectoryToSameDirectory() throws AuthenticationException {
		// 1º Crear el workspace
				GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
				workspace.persist();
				// 2º Crear el proyecto
				GDriveProject project = new GDriveProject("project2", workspace.getName(), user, credentials);
				project.persist();
				
				//3º Crear el directorio
				GDriveDirectory dir=new GDriveDirectory("D", workspace.getName(), project.getName(), user, credentials);
				dir.persist();
				
				//4º Mover al mismo directorio
				boolean move=dir.move(dir, true);
				assertFalse(move);
	}
	// Mover
	// archivos###################################################################################

	// Copiar archivo a otro directorio
	@Test
	public void testMoveFileToDirectory() throws AuthenticationException {
		System.out.println("Test move file to directory ==================================================");
		// 1º Crear el workspace

		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", user, credentials);
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();

		// 3º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(), user,
				credentials);
		directory.persist();
		// 4º Crear el archivo

		GDriveFile file = new GDriveFile("file1.txt", workspace.getName(), project.getName(), user, credentials);
		file.persist();

		// Copiar el archivo al directorio directory1
		boolean move = file.move(directory, true);
		assertTrue(move);

		// Comprobar que tanto project1 como directory1 contiene file1.txt
		assertTrue(directory.list().toString().contains(file.getName()));
		assertTrue(project.list().toString().contains(file.getName()));
	}

	//Intentar mover el archivo a otro proyecto


	// Intentar copiar el archivo a otro workspace
	@Test
	public void testCopyFileToWorkspace() throws AuthenticationException {
		System.out.println("Test copy file to workspace ===========================================================");
		// 1º Crear el workspace origen

		GDriveWorkspace workspace = new GDriveWorkspace("workspace5", user, credentials);
		workspace.persist();
		// 2º Crear el workspace destino
		GDriveWorkspace dest = new GDriveWorkspace("workspace6", user, credentials);
		dest.persist();
		// 3º Crear el proyecto origen
		GDriveProject project = new GDriveProject("project1", workspace.getName(), user, credentials);
		project.persist();


		// 4º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(), user,
				credentials);
		directory.persist();
		// 5º Crear el archivo
		GDriveFile file = new GDriveFile("file1.txt", workspace.getName(), project.getName(), user, credentials);
		file.persist();

		// Mover el archivo al otro workspace
		boolean move = file.move(dest, true);
		assertFalse(move);

	}
	
	//Download y upload ######################################################################
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
//TODO
		FSWorkspace wlocal = new FSWorkspace("wfile", user);
		// DriveQuickstart.uploadWorkspace(wlocal.getName(), user,credentials);
		
		boolean upload=wlocal.uploadWorkspaceToGdrive(credentials,true);
		assertTrue(upload);
		
		//Comprobamos que existe el workspace en google drive
		GDriveWorkspace gw=new GDriveWorkspace(wlocal.getName(), user, credentials);
		assertTrue(gw.exist());
	}

}
