package es.us.isa.ideas.repo;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import es.us.isa.ideas.repo.exception.AuthenticationException;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.gdrive.DriveQuickstart;
import es.us.isa.ideas.repo.gdrive.GDriveDirectory;
import es.us.isa.ideas.repo.gdrive.GDriveFile;
import es.us.isa.ideas.repo.gdrive.GDriveProject;
import es.us.isa.ideas.repo.gdrive.GDriveWorkspace;
import es.us.isa.ideas.repo.impl.fs.FSFile;
import es.us.isa.ideas.repo.impl.fs.FSProject;
import es.us.isa.ideas.repo.impl.fs.FSWorkspace;
import junit.framework.Assert;

public class GDriveTest {
	private static String userAuthenticated() {
		DummyAuthenticationManagerDelegate authDelegate = new DummyAuthenticationManagerDelegate("yo");
		IdeasRepo.setAuthManagerDelegate(authDelegate);
		return authDelegate.getAuthenticatedUserId();
	}

	// Crea una carpeta de repositorio antes de comenzar los test
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		File fileMetadata = new File();
		fileMetadata.setName(userAuthenticated());
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		DriveQuickstart.driveService().files().create(fileMetadata).setFields("id").execute();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		GDriveWorkspace gdworkspace = new GDriveWorkspace("workspace1", userAuthenticated());
		String id = DriveQuickstart.getRepoFolder(gdworkspace.getOwner()).getId();
		DriveQuickstart.driveService().files().delete(id).execute();
		FSWorkspace w = new FSWorkspace("workspace4", userAuthenticated());
		w.delete();
		FSWorkspace w5 = new FSWorkspace("workspace5", userAuthenticated());
		w5.delete();
		FSWorkspace w6 = new FSWorkspace("workspace6", userAuthenticated());
		w6.delete();


		
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
		GDriveWorkspace gdworkspace = new GDriveWorkspace("workspace1", userAuthenticated());
		gdworkspace.persist();
		assertTrue(gdworkspace.exist());
	}

	@Test
	public void testRepoFolder() throws IOException, GeneralSecurityException {
		GDriveWorkspace gdworkspace = new GDriveWorkspace("workspace1", userAuthenticated());
		System.out.println("Test repo folder ====================");
		System.out.println(DriveQuickstart.getRepoFolder(gdworkspace.getOwner()).toString());
		assertTrue(gdworkspace.exist());
	}

	@Test
	public void testDelete() {
		GDriveWorkspace gw = new GDriveWorkspace("workspace2", userAuthenticated());
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
		GDriveWorkspace gw = new GDriveWorkspace("workspace3", userAuthenticated());
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
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		boolean save = project.persist();
		// Comprobar que existe
		assertTrue(project.exist());
		System.out.println(project.getName());

	}

	// Intentar guardar un proyecto en un workspace que no existe
	@Test
	public void testSaveProjectFail() throws AuthenticationException {
		System.out.println("Test save project fail ==================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace56", userAuthenticated());
		assertTrue(!workspace.exist());
		GDriveProject project = new GDriveProject("project2", workspace.getName(), userAuthenticated());
		boolean save = project.persist();
		assertTrue(save == false);

	}

	@Test
	public void testListProject() throws AuthenticationException {
		System.out.println("Test list project ===========================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		GDriveProject project = new GDriveProject("project1", "workspace4", userAuthenticated());
		project.persist();
		System.out.println(project.list().toString());
	}

	@Test
	public void testDeleteProject() throws AuthenticationException {
		System.out.println("Test delete project ===================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		GDriveProject project = new GDriveProject("project1", "workspace4", userAuthenticated());
		project.persist();

		boolean delete = project.delete();
		assertTrue(delete == true);
		assertTrue(!project.exist());

	}

	// Intentar borrar un proyecto que no existe
	@Test
	public void testDeleteProjectFail() throws AuthenticationException {
		System.out.println("Test delete project fail =====================================");
		GDriveProject project = new GDriveProject("project3", "workspace6", userAuthenticated());

		boolean delete = project.delete();
		assertTrue(delete == false);
	}

	// Intentar borrar un proyecto que no existe
	@Test
	public void testDeleteProjectFail2() throws AuthenticationException {
		System.out.println("Test delete project fail 2 ======================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		GDriveProject project = new GDriveProject("project64", "workspace4", userAuthenticated());
		project.delete();
	}

	// Mover un proyecto a otro workspace
	@Test
	public void testMoveProject() throws AuthenticationException {
		System.out.println("Test move project ===================================");
		// 1º Se crea el workspace origen
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Se crea el workspace destino
		GDriveWorkspace dest = new GDriveWorkspace("workspace5", userAuthenticated());
		dest.persist();
		// 3º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		boolean save = project.persist();
		// 4º Mover el proyecto
		project.move(dest, false);

		// Comprobar que ahora esta en el workspace destino
		assertTrue(dest.list().toString().contains(project.getName()));

	}

	// Intentar mover un proyecto a un workspace que no existe
	@Test
	public void testMoveProjectFail() throws AuthenticationException {
		System.out.println("Test move project fail ==========================================");
		GDriveWorkspace w = new GDriveWorkspace("Workspace4", userAuthenticated());
		GDriveProject project = new GDriveProject("project1", w.getName(), userAuthenticated());
		project.persist();

		boolean move = project.move(w, false);
		// Comprobar que no se ha podido realizar la operacion
		assertTrue(!move);
	}

	// GDriveFile###############################################################################
	@Test
	public void testSaveFile() throws AuthenticationException {

		System.out.println("Test save file ===================================================");
		// 1º Se crea el workspace
		FSWorkspace w = new FSWorkspace("workspace4", userAuthenticated());
		w.persist();
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto
		FSProject p = new FSProject("project1", workspace.getName(), userAuthenticated());
		p.persist();
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();

		// 3º Se crea el fichero

		FSFile fsfile = new FSFile("datos.txt", workspace.getName(), project.getName(), userAuthenticated());
		fsfile.persist();

		// 4º Se sube el fichero
		// PARA SUBIR UN FICHERO HACE FALTA QUE EXISTA EN EL REPOSITORIO
		GDriveFile file = new GDriveFile(fsfile.getName(), workspace.getName(), project.getName(), userAuthenticated());
		boolean save = file.persist();

		assertTrue(save);
		// Comprobar que el fichero existe
		assertTrue(file.exist());

	}

	// Guardar un archivo que no este en el repositorio
	@Test
	public void testSaveFileFailed() throws AuthenticationException {

		System.out.println("Test save file failed ===================================================");

		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto

		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();

		// 3º Se sube el fichero
		GDriveFile file = new GDriveFile("datos3.txt", workspace.getName(), project.getName(), userAuthenticated());
		boolean save = file.persist();
		assertTrue(!save);
		assertTrue(!file.exist());

	}

	// Eliminar un fichero
	@Test
	public void testDeleteFile() throws AuthenticationException {
		System.out.println("Test delete file ===================================================");
		// 1º Se crea el workspace
		FSWorkspace w = new FSWorkspace("workspace4", userAuthenticated());
		w.persist();
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto
		FSProject p = new FSProject("project1", workspace.getName(), userAuthenticated());
		p.persist();
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();

		// 3º Se crea el fichero

		FSFile fsfile = new FSFile("datos.txt", workspace.getName(), project.getName(), userAuthenticated());
		fsfile.persist();

		// 4º Se sube el fichero
		// PARA SUBIR UN FICHERO HACE FALTA QUE EXISTA EN EL REPOSITORIO
		GDriveFile file = new GDriveFile(fsfile.getName(), workspace.getName(), project.getName(), userAuthenticated());
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

		System.out.println("Test save file failed ===================================================");

		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto

		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();

		// 3º Se sube el fichero
		GDriveFile file = new GDriveFile("datos3.txt", workspace.getName(), project.getName(), userAuthenticated());

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
		FSWorkspace w = new FSWorkspace("workspace4", userAuthenticated());
		w.persist();
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto
		FSProject p = new FSProject("project1", workspace.getName(), userAuthenticated());
		p.persist();
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();

		// 3º Se crea el fichero

		FSFile fsfile = new FSFile("datos4.txt", workspace.getName(), project.getName(), userAuthenticated());
		fsfile.persist();

		// 4º Se sube el fichero
		// PARA SUBIR UN FICHERO HACE FALTA QUE EXISTA EN EL REPOSITORIO
		GDriveFile file = new GDriveFile(fsfile.getName(), workspace.getName(), project.getName(), userAuthenticated());
		file.persist();
		// Escribir en el archivo
		boolean write=file.write("DATOS");
		assertTrue(write);
		GDriveFile newFile = new GDriveFile(fsfile.getName(), workspace.getName(), project.getName(), userAuthenticated());
		
		// leer el archivo
		assertEquals("DATOS", newFile.readAsString());
	}
	//Escribir y leer un fichero como un byte
	@Test
	public void testWriteReadFileAsByte() throws AuthenticationException {
		System.out.println("Test write & read file as byte ===================================================");
		// 1º Se crea el workspace
		FSWorkspace w = new FSWorkspace("workspace4", userAuthenticated());
		w.persist();
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto
		FSProject p = new FSProject("project1", workspace.getName(), userAuthenticated());
		p.persist();
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();

		// 3º Se crea el fichero

		FSFile fsfile = new FSFile("datos5.txt", workspace.getName(), project.getName(), userAuthenticated());
		fsfile.persist();

		// 4º Se sube el fichero
		// PARA SUBIR UN FICHERO HACE FALTA QUE EXISTA EN EL REPOSITORIO
		GDriveFile file = new GDriveFile(fsfile.getName(), workspace.getName(), project.getName(), userAuthenticated());
		file.persist();
		// Escribir en el archivo
		boolean write=file.write("DATOS".getBytes());
		assertTrue(write);
		GDriveFile newFile = new GDriveFile(fsfile.getName(), workspace.getName(), project.getName(), userAuthenticated());
		
		// leer el archivo
		assertArrayEquals("DATOS".getBytes(), newFile.readAsBytes());
	}
	//Escribir y leer en un fichero que no exista
	@Test
	public void testWriteReadFileFail() throws AuthenticationException {
		System.out.println("Test write & read file fail ==========================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();
		GDriveFile file = new GDriveFile("datos6.txt", workspace.getName(), project.getName(), userAuthenticated());
		assertTrue(!file.exist());
		boolean write=file.write("DATOS");
		assertTrue(!write);
		assertNull(file.readAsString());
		
	}
	//Escribir como bytes en un fichero que no exista
	@Test
	public void testWriteFileAsByteFail() throws AuthenticationException {
		System.out.println("Test write file as byte fail =========================================");
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();
		GDriveFile file = new GDriveFile("datos6.txt", workspace.getName(), project.getName(), userAuthenticated());
		assertTrue(!file.exist());
		boolean write=file.write("DATOS".getBytes());
		assertTrue(!write);
	}

	// GDriveDirectory
	// ################################################################################

	// Guardar directory
	@Test
	public void testSaveDirectory() throws AuthenticationException {
		System.out.println("Test save directory ==================================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();
		// 3º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(),
				userAuthenticated());
		boolean save = directory.persist();
		assertTrue(directory.exist());
		assertTrue(save);
	}

	// Intentar guardar el directory en un proyecto que no exista
	@Test
	public void testSaveDirectoryFail() throws AuthenticationException {
		System.out.println("Test save directory fail ===========================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project14", workspace.getName(), userAuthenticated());
		// 3º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(),
				userAuthenticated());
		boolean save = directory.persist();
		assertTrue(!directory.exist());
		assertTrue(!save);
	}

	// Eliminar una carpeta
	@Test
	public void testDeleteDirectory() throws AuthenticationException {
		System.out.println("Test delete directory ==================================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();
		// 3º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(),
				userAuthenticated());
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
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();
		// 3º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(),
				userAuthenticated());
		boolean delete = directory.delete();
		assertTrue(!delete);
	}

	// Mover la carpeta de un proyecto a otro
	@Test
	public void testMoveDirectory() throws AuthenticationException {
		System.out.println("Test move directory ==================================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto origen
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();
		// 3º Crear el proyecto destino
		GDriveProject dest = new GDriveProject("Proyecto destino", workspace.getName(), userAuthenticated());
		dest.persist();
		// 4º Crear la carpeta
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(),
				userAuthenticated());
		directory.persist();
		// 5º Mover la carpeta al proyecto destino
		directory.move(dest, true);
		System.out.println(dest.list().getChildren());
		// Comprobamos que el directorio existe dentro del proyecto dest
		assertTrue(dest.list().toString().contains(directory.getName()));
	}

	// Intentar mover una carpeta a un proyecto con una carpeta con el mismo nombre
	@Test
	public void testMoveDirectoryFail() throws AuthenticationException {
		System.out.println("Test move directory fail==================================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto origen
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();
		// 3º Crear el proyecto destino
		GDriveProject dest = new GDriveProject("Proyecto destino", workspace.getName(), userAuthenticated());
		dest.persist();
		// 4º Crear las carpetas
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(),
				userAuthenticated());
		directory.persist();
		GDriveDirectory directory2 = new GDriveDirectory("directory1", workspace.getName(), dest.getName(),
				userAuthenticated());
		directory2.persist();
		// 5º Mover la carpeta al proyecto destino
		boolean move = directory.move(dest, true);
		// Comprobamos que no se ha podido realizar la operacion
		assertTrue(!move);
		// Comprobar que el proyecto destino contiene solo una carpeta
		assertTrue(dest.list().getChildren().size() == 1);

	}

	// Intentar mover una carpeta a otra que no sea un proyecto
	@Test
	public void testMoveDirectoryFail2() throws AuthenticationException {
		System.out.println("Test move directory fail 2==================================================");
		// 1º Crear el workspace
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto origen
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();
		// 3º Crear la carpeta
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(),
				userAuthenticated());
		directory.persist();
		// 4º Mover la carpeta al workspace
		boolean move = directory.move(workspace, true);
		// Comprobamos que no se ha podido hacer la operacion
		assertTrue(!move);

	}

	// Mover
	// archivos###################################################################################

	// Copiar archivo a otro directorio
	@Test
	public void testMoveFileToDirectory() throws AuthenticationException {
		System.out.println("Test move file to directory ==================================================");
		// 1º Crear el workspace
		FSWorkspace w = new FSWorkspace("workspace4", userAuthenticated());
		w.persist();
		GDriveWorkspace workspace = new GDriveWorkspace("workspace4", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();
		FSProject p = new FSProject(project.getName(), workspace.getName(), userAuthenticated());
		p.persist();
		// 3º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(),
				userAuthenticated());
		directory.persist();
		// 4º Crear el archivo
		FSFile f = new FSFile("file1.txt", workspace.getName(), project.getName(), userAuthenticated());
		f.persist();
		GDriveFile file = new GDriveFile("file1.txt", workspace.getName(), project.getName(), userAuthenticated());
		file.persist();

		// Copiar el archivo al directorio directory1
		boolean move = file.move(directory, true);
		assertTrue(move);

		// Comprobar que tanto project1 como directory1 contiene file1.txt
		assertTrue(directory.list().toString().contains(file.getName()));
		assertTrue(project.list().toString().contains(file.getName()));
	}

	// Mover el archivo a otro proyecto
	@Test
	public void testMoveFileToProject() throws AuthenticationException {
		System.out.println("Test move file to project ===========================================================");
		// 1º Crear el workspace
		FSWorkspace w = new FSWorkspace("workspace5", userAuthenticated());
		w.persist();
		GDriveWorkspace workspace = new GDriveWorkspace("workspace5", userAuthenticated());
		workspace.persist();
		// 2º Crear el proyecto origen
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();
		FSProject p = new FSProject(project.getName(), workspace.getName(), userAuthenticated());
		p.persist();
		//3º Crear el proyecto destino
		GDriveProject dest=new GDriveProject("Destino", workspace.getName(), userAuthenticated());
		dest.persist();
		// 4º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(),
				userAuthenticated());
		directory.persist();
		// 5º Crear el archivo
		FSFile f = new FSFile("file1.txt", workspace.getName(), project.getName(), userAuthenticated());
		f.persist();
		GDriveFile file = new GDriveFile("file1.txt", workspace.getName(), project.getName(), userAuthenticated());
		file.persist();
		
		//Mover el archivo a otro proyecto
		boolean move=file.move(dest, false);
		assertTrue(move);
		
		assertTrue(dest.list().toString().contains(file.getName()));
		assertTrue(!project.list().toString().contains(file.getName()));
	}
	
	//Copiar el archivo a otro workspace
	@Test
	public void testCopyFileToWorkspace() throws AuthenticationException {
		System.out.println("Test copy file to workspace ===========================================================");
		// 1º Crear el workspace origen
		FSWorkspace w = new FSWorkspace("workspace5", userAuthenticated());
		w.persist();
		GDriveWorkspace workspace = new GDriveWorkspace("workspace5", userAuthenticated());
		workspace.persist();
		// 2º Crear el workspace destino
		GDriveWorkspace dest=new GDriveWorkspace("workspace6", userAuthenticated());
		dest.persist();
		// 3º Crear el proyecto origen
		GDriveProject project = new GDriveProject("project1", workspace.getName(), userAuthenticated());
		project.persist();
		FSProject p = new FSProject(project.getName(), workspace.getName(), userAuthenticated());
		p.persist();
		
		// 4º Crear el directory
		GDriveDirectory directory = new GDriveDirectory("directory1", workspace.getName(), project.getName(),
				userAuthenticated());
		directory.persist();
		// 5º Crear el archivo
		FSFile f = new FSFile("file1.txt", workspace.getName(), project.getName(), userAuthenticated());
		f.persist();
		GDriveFile file = new GDriveFile("file1.txt", workspace.getName(), project.getName(), userAuthenticated());
		file.persist();
		
		//Mover el archivo al otro workspace
		boolean move=file.move(dest, true);
		assertTrue(move);
		//Comprobar que el archivo esta en el workspace y en el proyecto
		assertTrue(dest.list().toString().contains(file.getName()));
		assertTrue(project.list().toString().contains(file.getName()));
	}

}
