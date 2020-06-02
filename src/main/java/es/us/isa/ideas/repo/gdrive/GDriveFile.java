package es.us.isa.ideas.repo.gdrive;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;

import es.us.isa.ideas.repo.File;
import es.us.isa.ideas.repo.IdeasRepo;
import es.us.isa.ideas.repo.exception.AuthenticationException;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.operation.Listable;

public class GDriveFile extends File {
	private static final Logger LOGGER = Logger.getLogger(GDriveFile.class.getName());
	private Drive credentials;

	public GDriveFile(String name, String workspace, String project, String owner, Drive credentials) {
		super(name, workspace, project, owner);
		this.credentials = credentials;
	}

	public Drive getCredentials() {
		return credentials;
	}

	public void setCredentials(Drive credentials) {
		this.credentials = credentials;
	}

	@Override
	public boolean write(byte[] content) throws AuthenticationException {
		boolean res = false;
		try {
			com.google.api.services.drive.model.File file = DriveQuickstart.getFileByName(this.getName(), null,
					this.getProject(), this.getWorkspace(), this.getOwner(), this.credentials);
			if (file == null) {
				LOGGER.log(Level.INFO, "File " + this.getName() + " does not exist.");

			} else {
				DriveQuickstart.writeFileAsByte(file.getId(), content, credentials);
				res = true;
			}
		} catch (IOException | GeneralSecurityException e) {

			e.printStackTrace();
		}
		return res;
	}

	@Override
	protected boolean writeImpl(String content) {
		boolean res = false;
		try {
			com.google.api.services.drive.model.File parentFolder = getParentFolder();
			com.google.api.services.drive.model.File file = DriveQuickstart.getFileByName(getFileName(),
					parentFolder.getId(), this.credentials);
			if (file == null) {

				LOGGER.log(Level.INFO, "File " + this.getName() + " does not exist.");

			} else {
				DriveQuickstart.writeFile(file.getId(), content, credentials);
				res = true;
			}

		} catch (IOException | GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}

	@Override
	protected boolean moveImpl(Listable dest, boolean copy) {
		boolean res = false;
		try {
			com.google.api.services.drive.model.File parentFolder = getParentFolder();
			com.google.api.services.drive.model.File file = DriveQuickstart.getFileByName(getFileName(),
					parentFolder.getId(), this.credentials);
			if (file == null) {
				LOGGER.log(Level.INFO, "File " + this.getName() + " does not exist.");

			} else {
				String directoryName = null;
				// System.out.println(IdeasRepo.get().getObjectFullUri(dest));
				String[] s1 = IdeasRepo.get().getObjectFullUri(dest).split("//");
				String[] s2 = s1[1].split("/");
				com.google.api.services.drive.model.File folder = null;

				if (dest instanceof GDriveProject) {
					folder = DriveQuickstart.getProjectByName(s2[s2.length - 1], s2[s2.length - 2], s2[s2.length - 3],
							this.credentials);
				} else if (dest instanceof GDriveDirectory) {
					folder = DriveQuickstart.getDirectoryByName(s2[s2.length - 1], s2[s2.length - 2], s2[s2.length - 3],
							s2[s2.length - 4], this.credentials);
					directoryName = s2[s2.length - 1];
//					fileInFolder = DriveQuickstart.getFileByName(this.getName(), directoryName, this.getProject(),
					// this.getWorkspace(), this.getOwner(), this.getCredentials());
				} else {
					LOGGER.log(Level.INFO, "The target folder must be a directory");
				}
				if (folder == null) {
					LOGGER.log(Level.INFO, "Folder " + dest.getClass().getName() + " does not exist.");

				} else {

					if (copy) {
						DriveQuickstart.copyFileToFolder(file.getId(), folder.getId(), credentials);
					} else {
						DriveQuickstart.moveFileToFolder(file.getId(), folder.getId(), credentials);

					}
					res = true;

				}
			}

		} catch (IOException | GeneralSecurityException | ObjectClassNotValidException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	protected boolean deleteImpl() {
		boolean res = false;
		// Obtenemos el fichero
		try {
			com.google.api.services.drive.model.File parentFolder = getParentFolder();
			com.google.api.services.drive.model.File file = DriveQuickstart.getFileByName(getFileName(),
					parentFolder.getId(), this.credentials);
			if (file == null) {
				LOGGER.log(Level.INFO, "File " + this.getName() + " does not exist.");

			} else {
				this.credentials.files().delete(file.getId()).execute();
				res = true;
			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		
		}

		return res;
	}

	@Override
	protected boolean persistImpl() {
		boolean res = false;
		// workspace/proyect/directory/file

		try {
			System.out.println(this.getName());
			com.google.api.services.drive.model.File parentFolder = getParentFolder();

			// Comprobamos que existe la carpeta donde se va a guardar el fichero
			if (parentFolder == null) {
				LOGGER.log(Level.INFO, "The folder does not exist.");
			}

			// Comprobamos que no exista un fichero con el mismo nombre

			com.google.api.services.drive.model.File file = DriveQuickstart.getFileByName(getFileName(),
					parentFolder.getId(), this.getCredentials());
			if (file != null) {
				LOGGER.log(Level.INFO, "File " + this.getName() + " already exist");
				return false;
			} else {
				// Creamos el fichero
				com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();

				String type = null;

				if (this.getName().contains(".txt")) {
					type = "text/plain";
				}
				// Si el formato no coincide con .txt la api de google drive detecta el formato
				// automaticamente

				fileMetadata.setName(getFileName());
				fileMetadata.setParents(Collections.singletonList(parentFolder.getId()));

				// java.io.File filePath = new
				// java.io.File(IdeasRepo.get().getObjectFullUri(this));

				// Se crea el archivo temporal
				java.io.File tempFile = java.io.File.createTempFile("temp_file", null);
				java.io.File filePath = new java.io.File(tempFile.getAbsolutePath());

				FileContent mediaContent = new FileContent(type, filePath);

				// Guardamos el fichero
				this.credentials.files().create(fileMetadata, mediaContent).setFields("id").execute();

				// Borrar el fichero temporal
				tempFile.delete();
				res = true;
			}

		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {

			LOGGER.log(Level.INFO, "Failed creating new file. ");
			e.printStackTrace();
		}
		return res;
	}

	@Override
	protected String readAsStringImpl() {
		String res = "";
		try {
			com.google.api.services.drive.model.File parentFolder = getParentFolder();
			com.google.api.services.drive.model.File file = DriveQuickstart.getFileByName(getFileName(),
					parentFolder.getId(), this.credentials);
			if (file == null) {
				LOGGER.log(Level.INFO, "File " + this.getName() + " does not exist.");
				res = null;
			} else {

				res = res + DriveQuickstart.download(file.getId(), this.credentials);
			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;

	}

	@Override
	protected byte[] readAsBytesImpl() {

		return readAsStringImpl().getBytes();
	}

	public boolean exist() {
		boolean res = false;
		try {
			com.google.api.services.drive.model.File parentFolder = getParentFolder();
			if (DriveQuickstart.getFileByName(getFileName(), parentFolder.getId(), this.credentials) != null) {
				res = true;
			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}

		return res;
	}

	private com.google.api.services.drive.model.File getParentFolder() throws IOException, GeneralSecurityException {
		// Hacer un split del nombre
		String[] splitName = this.getName().split("\\\\");
		com.google.api.services.drive.model.File parentFolder = null;
		String directoryName = "";

		// El directorio (carpeta padre) sera split.lengh - 2
		// workspace/project/directory1\directory2\...\file
		if (splitName.length > 2) {
			directoryName=this.getName().replaceFirst("\\\\[^\\\\]+?$", "");

			parentFolder = DriveQuickstart.getDirectoryByName(directoryName, this.getProject(), this.getWorkspace(),
					this.getOwner(), this.getCredentials());
		} // workspace/project/directory\file
		else if (splitName.length > 1) {
			directoryName = splitName[splitName.length - 2];
			// Hay que buscar el directory por el parentDirectory
			parentFolder = DriveQuickstart.getDirectoryByName(directoryName, this.getProject(), this.getWorkspace(),
					this.getOwner(), this.getCredentials());

		} else {// workspace/project/file
			parentFolder = DriveQuickstart.getProjectByName(this.getProject(), this.getWorkspace(), this.getOwner(),
					this.getCredentials());
		}
		return parentFolder;
	}

	private String getFileName() {

		String[] splitName = this.getName().split("\\\\");
		return splitName[splitName.length - 1];
	}

}
