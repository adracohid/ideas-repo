package es.us.isa.ideas.repo;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import es.us.isa.ideas.repo.exception.AuthenticationException;
import es.us.isa.ideas.repo.exception.BadUriException;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.impl.fs.FSDirectory;
import es.us.isa.ideas.repo.impl.fs.FSFile;
import es.us.isa.ideas.repo.impl.fs.FSNode;
import es.us.isa.ideas.repo.impl.fs.FSProject;
import es.us.isa.ideas.repo.impl.fs.FSRepo;
import es.us.isa.ideas.repo.impl.fs.FSWorkspace;

public class FSTest {

	private static String userAuthenticated() {
		DummyAuthenticationManagerDelegate authDelegate = new DummyAuthenticationManagerDelegate("yo");
		IdeasRepo.setAuthManagerDelegate(authDelegate);
		return authDelegate.getAuthenticatedUserId();
	}

	private FSWorkspace workspace2() throws AuthenticationException {

		FSWorkspace workspace2 = new FSWorkspace("workspace2", userAuthenticated());

		return workspace2;
	}

	// Antes de hacer los test creamos el workspace2
	@BeforeClass
	public static void setupClass() throws AuthenticationException {

		FSWorkspace workspace2 = new FSWorkspace("workspace2", userAuthenticated());
		workspace2.persist();

	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@AfterClass
	public static void teardownClass() throws AuthenticationException, BadUriException {
		FSWorkspace workspace2 = new FSWorkspace("workspace2", userAuthenticated());
		workspace2.delete();
		FSFile history = (FSFile) Facade.getFileFromUri("//.history", userAuthenticated());
		history.delete();
	}
	
	@Test
	public void testListWorkspace() throws AuthenticationException {
		System.out.println("LISTAR WORKSPACE======================================");
		FSWorkspace workspace=new FSWorkspace("W1", userAuthenticated());
		workspace.persist();
		FSProject p=new FSProject("p1", workspace.getName(), userAuthenticated());
		p.persist();
		System.out.println("Nodo padre: "+workspace.list().getTitle());
		System.out.println("\n Hijos: ");
		for(Node n:workspace.list().getChildren()) {
			System.out.println(n.getTitle());
		}
	}
	// FSWorkspace
	// ######################################################################
	@Test
	public void testSaveWorkspace() throws AuthenticationException, ObjectClassNotValidException {

		FSWorkspace workspace = new FSWorkspace("workspace1", userAuthenticated());

		workspace.persist();

		IdeasRepo irepo = new IdeasRepo();
		File ws = new File(IdeasRepo.get().getObjectFullUri(workspace));
		
		assertTrue(ws.exists());
		System.out.println("\nTest Save Workspace =================================");
		System.out.println("URI Workspace1: " + irepo.getObjectFullUri(workspace));

	}

	// Borrar un workspace
	@Test
	public void testDeleteWorkspace() throws AuthenticationException, ObjectClassNotValidException {
		FSRepo repo = new FSRepo();
		FSWorkspace w1 = new FSWorkspace("w1", userAuthenticated());

		w1.persist();
		System.out.println("\nTest Delete Workspace==================================");
		File ws = new File(IdeasRepo.get().getObjectFullUri(w1));
		assertTrue(ws.exists());
		System.out.println("Woskpaces before delete w1: " + repo.getWorkspaces(userAuthenticated()));
		w1.delete();
		assertTrue(!ws.exists());
		System.out.println("Workspaces after delete w1: " + repo.getWorkspaces(userAuthenticated()));

	}

	// Borrar un workspace que no esta guardado
	@Test
	public void testDeleteWorkspaceFailed() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("\nTest Delete Workspace Failed =====================");
		FSWorkspace w2 = new FSWorkspace("w2", userAuthenticated());
		File ws = new File(IdeasRepo.get().getObjectFullUri(w2));
		//Comprobamos que no existe
		assertTrue(!ws.exists());
		boolean delete=w2.delete();
		//Comprobamos que no se ha podido eliminar
		assertTrue(delete==false);
	}


	// Actualizar un workspace
	@Test
	public void testUpdateWorkspace()
			throws IOException, AuthenticationException, BadUriException, ObjectClassNotValidException {
		System.out.println("Test update workspace =================================");
		FSRepo repo = new FSRepo();
		repo.saveSelectedWorkspace(workspace2().getName(), userAuthenticated());

		FSFile history = (FSFile) Facade.getFileFromUri("//.history", userAuthenticated());
		System.out.println(history.readAsString());

		// Comprobar que se ha creado el fichero .history
		File ws = new File(IdeasRepo.get().getObjectFullUri(history));
		assertTrue(ws.exists());

	}

	// Get selected workspace
	@Test
	public void testGetSelectedWorkspace() throws AuthenticationException {
		System.out.println("Get selected workspace ================================");
		FSRepo repo = new FSRepo();
		try {
			// Se ejecuta saveSelectedWorkspace para que no de error
			repo.saveSelectedWorkspace(workspace2().getName(), userAuthenticated());
			String ws = repo.getSelectedWorkspace(userAuthenticated());
			System.out.println(ws);
			// Comprobar que el workspace seleccionado este en los workspaces del
			// repositorio
			assertTrue(repo.getWorkspaces(userAuthenticated()).contains(ws));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// FSProject
	// #######################################################################
	// Crear un proyecto y guardarlo
	@Test
	public void testSaveProject() throws AuthenticationException, ObjectClassNotValidException {
		FSWorkspace w1 = new FSWorkspace("workspace1", userAuthenticated());
		w1.persist();
		FSProject project = new FSProject("proyecto1", "workspace1", userAuthenticated());
		project.persist();
		System.out.println("\nTest Save Project===================================");
		File ws = new File(IdeasRepo.get().getObjectFullUri(project));
		assertTrue(ws.exists());
		assertTrue(w1.list().getChildren().toString().contains(project.getName()));
	}

	// Intentar guardar un proyecto en un workspace que no existe
	@Test
	public void testSaveProjectFailed() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("\nTest Save Project Failed ==============================");
		FSWorkspace w = new FSWorkspace("workspace f", userAuthenticated());
		FSProject project = new FSProject("proyecto fallido", w.getName(), userAuthenticated());
		boolean save=project.persist();
		//Comprobamos que no se ha podido guardar
		assertTrue(save==false);
		//Comprobamos que no existe el fichero
		File ws = new File(IdeasRepo.get().getObjectFullUri(project));
		assertTrue(!ws.exists());
	}

	// Mover el proyecto1 al workspace2
	@Test
	public void testMoveProject() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("\nTest move project =============================");
		FSProject project = new FSProject("proyecto1", "workspace1", userAuthenticated());
		FSProject copy = new FSProject(project.getName(), workspace2().getName(), userAuthenticated());

		project.move(copy, true);
		System.out.println(workspace2().list().getChildren().toString());
		// Como no cambia la uri hay que cambiarla manualmente
		project.setWorkspace(workspace2().getName());
		Assert.assertEquals(project.getWorkspace(), workspace2().getName());
	}

	// Eliminar proyecto
	@Test
	public void testDeleteProject() throws AuthenticationException {
		System.out.println("\nTest Delete Project =========================");
		FSWorkspace w1 = new FSWorkspace("workspace1", userAuthenticated());
		FSProject project = new FSProject("Proyecto2", "workspace1", userAuthenticated());
		project.persist();
		project.delete();

		// Asegurar que ya no existe el proyecto en workspace1
		assertTrue(!w1.list().getChildren().contains(project));
	}

	// Intentar eliminar un proyecto que no existe
	@Test
	public void testDeleteProjectFailed() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("\nTest Delete Project Failed=========================");
		FSProject p = new FSProject("Proyecto", workspace2().getName(), userAuthenticated());
		File ws = new File(IdeasRepo.get().getObjectFullUri(p));
		assertTrue(!ws.exists());
		assertTrue(p.delete() == false);

	}

	// FSDirectory
	// #####################################################################
	@Test
	public void testSaveFolder() throws AuthenticationException, ObjectClassNotValidException {

		FSDirectory folder = new FSDirectory("new directory", "workspace1", "proyecto1", userAuthenticated());
		FSWorkspace w1 = new FSWorkspace("workspace1", userAuthenticated());
		folder.persist();
		System.out.println("\nTest save folder===================================");

		// Asegurarse que workspace1 contiene a la carpeta
		Assert.assertTrue(w1.list().getChildren().toString().contains("new directory"));

	}

	// Intentar guardar una carpeta en un workspace que no existe
	@Test
	public void testSaveFolderFailed() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("Test save folder failed ===========================");
		FSDirectory d1 = new FSDirectory("Directorio", "workspace3", "proyecto1", userAuthenticated());
		boolean persist = d1.persist();
		// Comprobar que no existe
		File ws = new File(IdeasRepo.get().getObjectFullUri(d1));
		assertTrue(!ws.exists());
		assertTrue(persist == false);
	}

	// Mover una carpeta de un proyecto a otro
	@Test
	public void testMoveFolder() throws AuthenticationException, ObjectClassNotValidException {
		// Primero creamos el proyecto
		FSProject project2 = new FSProject("project2", "workspace1", userAuthenticated());
		project2.persist();
		// Obtenemos la carpeta new directory
		System.out.println("\nTest Move Folder==========================================");
		FSDirectory folder = new FSDirectory("new directory", "workspace1", "proyecto1", userAuthenticated());
		FSDirectory copy = new FSDirectory(folder.getName(), folder.getWorkspace(), "project2", userAuthenticated());
		folder.move(copy, false);
		folder.setProject("project2");
		assertEquals(folder.getProject(), project2.getName());

	}

	@Test
	public void testDeleteFolder() throws AuthenticationException, ObjectClassNotValidException {
		FSProject project = new FSProject("proyecto2", workspace2().getName(), userAuthenticated());
		project.persist();
		FSDirectory folder = new FSDirectory("new directory", workspace2().getName(), project.getName(),
				userAuthenticated());
		folder.persist();

		System.out.println("\nTest Delete Folder =================================");

		folder.delete();
		File ws = new File(IdeasRepo.get().getObjectFullUri(folder));
		assertTrue(!ws.exists());
		assertTrue(!workspace2().list().getChildren().contains(folder));
	}

	// Intentar eliminar una carpeta que no existe
	@Test
	public void testDeleteFolderFailed() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("Test delete Folder Failed ============================");
		FSDirectory d1 = new FSDirectory("Directory", "workspace1", "proyecto1", userAuthenticated());
		File ws = new File(IdeasRepo.get().getObjectFullUri(d1));
		assertTrue(!ws.exists());
		boolean delete = d1.delete();
		assertTrue(delete == false);
	}

	// FSFile
	// ##########################################################################

	@Test
	public void testSaveFile() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("Test save file ============================================");
		FSProject p1 = new FSProject("File project", workspace2().getName(), userAuthenticated());
		p1.persist();
		FSFile file1 = new FSFile("file1.jpg", workspace2().getName(), p1.getName(), userAuthenticated());
		file1.persist();
		// Comprobar que existe
		File ws = new File(IdeasRepo.get().getObjectFullUri(file1));
		assertTrue(ws.exists());
		// Comprobar que file1.jpg existe en el workspace2
		assertTrue(workspace2().list().getChildren().toString().contains(file1.getName()));
	}

	// Intentar guardar un archivo en un proyecto que no existe
	@Test
	public void testSaveFileFailed() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("\nTest save file failed =================================");
		FSFile file = new FSFile("Hola", workspace2().getName(), "Other proyect", userAuthenticated());
		File ws = new File(IdeasRepo.get().getObjectFullUri(file));
		assertTrue(!ws.exists());
		boolean persist = file.persist();
		assertTrue(persist == false);
	}

	@Test
	public void testDeleteFile() throws AuthenticationException {
		System.out.println("\nTest Delete File ========================================");
		FSFile file = new FSFile("Bellisimo.jpg", workspace2().getName(), "File project", userAuthenticated());
		file.delete();
		// Asegurar que el fichero no esta en workspace2
		assertTrue(!workspace2().list().getChildren().toString().contains(file.getName()));
	}

	// Intentar eliminar un archivo que no existe
	@Test
	public void testDeleteFileFailed() throws AuthenticationException {
		System.out.println("\nTest Delete File Failed ================================");
		FSFile file = new FSFile("nuevoFichero.txt", workspace2().getName(), "File project", userAuthenticated());
		try {
			// Comprobamos que no existe
			File ws = new File(IdeasRepo.get().getObjectFullUri(file));
			assertTrue(!ws.exists());
			boolean delete = file.delete();
			assertTrue(delete == false);
		} catch (ObjectClassNotValidException e) {
			e.printStackTrace();
		}
	}

	// Mover un archivo de un proyecto a otro
	@Test
	public void testMoveFile() throws AuthenticationException {
		System.out.println("\nTest move file ============================================");

		FSFile file = new FSFile("Fichero1.txt", workspace2().getName(), "File project", userAuthenticated());
		file.persist();
		FSProject p = new FSProject("UserProject", workspace2().getName(), userAuthenticated());
		p.persist();

		file.move(p, false);

		file.setProject(p.getName());
		Assert.assertEquals(file.getProject(), p.getName());
	}

	// Mover un archivo de un workspace a otro
	@Test
	public void testMoveFile2() throws AuthenticationException {
		System.out.println("\nTest move file 2 ===========================================");
		FSFile file = new FSFile("fichero2.txt", workspace2().getName(), "UserProject", userAuthenticated());
		file.persist();
		FSWorkspace w1 = new FSWorkspace("workspace1", userAuthenticated());
		file.move(w1, true);
		file.setWorkspace(w1.getName());
		assertEquals(file.getWorkspace(), w1.getName());
	}

	// Escribir y leer en un fichero como un string
	@Test
	public void testWriteFileAsString() throws AuthenticationException {
		System.out.println("\nTest write file as string ========================================");
		FSFile file = new FSFile("Prueba.txt", workspace2().getName(), "UserProject", userAuthenticated());
		file.persist();
		file.write("Hola");
		System.out.println("Contenido del fichero: " + file.readAsString());
		assertEquals("Hola", file.readAsString());

	}

	// Escribir y leer en un fichero como un byte[]
	@Test
	public void testWriteFileAsByte() throws AuthenticationException {
		System.out.println("\nTest write file as byte[] ================================");
		FSProject p = new FSProject("UserProject", workspace2().getName(), userAuthenticated());
		p.persist();
		FSFile file = new FSFile("Escritura.txt", workspace2().getName(), "UserProject", userAuthenticated());
		file.persist();
		file.write("Hola".getBytes());
		System.out.println("Contenido del fichero: " + file.readAsBytes());
		
		assertArrayEquals("Hola".getBytes(), file.readAsBytes());
	}

	// Escribir como string en un fichero que no exista
	@Test
	public void testWriteFileAsStringFailed() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("\nTest write file as string failed =========================");
		FSFile file = new FSFile("Prueba2.txt", workspace2().getName(), "UserProject", userAuthenticated());
		// Comprobamos que no existe
		File ws = new File(IdeasRepo.get().getObjectFullUri(file));
		assertTrue(!ws.exists());
		boolean write = file.write("hola");
		// Comprobamos que no se ha podido escribir
		assertTrue(write == false);

	}

	// Escribir como byte en un fichero que no exista
	@Test
	public void testWriteFileAsByteFailed() throws AuthenticationException, ObjectClassNotValidException {
		System.out.println("\nTest write file as byte[] failed ========================");
		FSFile file = new FSFile("Prueba2.txt", workspace2().getName(), "UserProject", userAuthenticated());
		// Comprobamos que no existe
		File ws = new File(IdeasRepo.get().getObjectFullUri(file));
		assertTrue(!ws.exists());
		boolean write=file.write("hola".getBytes());
		// Comprobamos que no se ha podido escribir
		assertTrue(write == false);
	}

	// Guardar un fichero en una carpeta
	@Test
	public void testSaveFileInDirectory() throws AuthenticationException {
		System.out.println("Test save file in directory =========================================");
		FSRepo repo = new FSRepo();
		FSFile c = new FSFile("Prueba2", workspace2().getName(), "UserProject", userAuthenticated());
		FSDirectory parent = new FSDirectory("Prueba", workspace2().getName(), "UserProject", userAuthenticated());
		parent.persist();
		repo.create(c, parent);

		assertTrue(workspace2().list().getChildren().toString().contains("Prueba"));
	}

}
