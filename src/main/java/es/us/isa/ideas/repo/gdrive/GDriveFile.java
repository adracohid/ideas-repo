package es.us.isa.ideas.repo.gdrive;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.http.FileContent;

import es.us.isa.ideas.repo.File;
import es.us.isa.ideas.repo.IdeasRepo;
import es.us.isa.ideas.repo.exception.AuthenticationException;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.operation.Listable;

public class GDriveFile extends File {
	private static final Logger LOGGER = Logger.getLogger(GDriveFile.class.getName());

	public GDriveFile(String name, String workspace, String project, String owner) {
		super(name, workspace, project, owner);
	}

	@Override
	public boolean write(byte[] content) throws AuthenticationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean writeImpl(String content) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean moveImpl(Listable dest, boolean copy) {
		boolean res = false;
		try {
			com.google.api.services.drive.model.File file = DriveQuickstart.getFileByName(this.getName(),
					this.getProject(), this.getWorkspace(), this.getOwner());
			if (file == null) {
				LOGGER.log(Level.INFO, "File " + this.getName() + " does not exist.");

			} else {

				System.out.println(IdeasRepo.get().getObjectFullUri(dest));
				String[] s1 = IdeasRepo.get().getObjectFullUri(dest).split("//");
				String[] s2 = s1[1].split("/");
				com.google.api.services.drive.model.File folder = null;
				if (dest instanceof GDriveProject) {
					folder = DriveQuickstart.getProjectByName(s2[s2.length - 1], s2[s2.length - 2], s2[s2.length - 3]);
				}
				if (dest instanceof GDriveWorkspace) {
					folder = DriveQuickstart.getWorkspaceByName(s2[s2.length - 1], s2[s2.length - 2]);
				}
				if (dest instanceof GDriveDirectory) {
					folder = DriveQuickstart.getDirectoryByName(s2[s2.length - 1], s2[s2.length - 2], s2[s2.length - 3],
							s2[s2.length - 4]);
				}
				if (folder == null) {
					LOGGER.log(Level.INFO, "Folder " + dest.getClass().getName() + " does not exist.");

				} else {
					if (!DriveQuickstart.getFilesByFolderId(folder.getId()).contains(file.getName())) {
						if (copy) {
							DriveQuickstart.copyFileToFolder(file.getId(), folder.getId());
						} else {
							DriveQuickstart.moveFileToFolder(file.getId(), folder.getId());

						}
						res = true;
					} else {
						LOGGER.log(Level.INFO, "Cannot move a file with the same name.");

					}
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
			com.google.api.services.drive.model.File file = DriveQuickstart.getFileByName(this.getName(),
					this.getProject(), this.getWorkspace(), this.getOwner());
			if (file == null) {
				LOGGER.log(Level.INFO, "File " + this.getName() + " does not exist.");

			} else {
				DriveQuickstart.driveService().files().delete(file.getId()).execute();
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
		try {
			// Obtener el workspace donde se va a guardar el archivo
			com.google.api.services.drive.model.File workspace = DriveQuickstart.getWorkspaceByName(this.getWorkspace(),
					this.getOwner());
			// Comprobar que el workspace existe
			if (workspace == null) {
				LOGGER.log(Level.INFO, "Workspace " + this.getWorkspace() + " does not exist.");

			} else {
				// Obtener el proyecto donde se va a guardar el fichero
				com.google.api.services.drive.model.File project = DriveQuickstart.getProjectByName(this.getProject(),
						this.getWorkspace(), this.getOwner());
				// Comprobar que el proyecto existe
				if (project == null) {
					LOGGER.log(Level.INFO, "Project " + this.getProject() + " does not exist.");

				} else {
					// Creamos el fichero
					com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();

					String type = null;

					if (this.getName().contains(".txt")) {
						type = "text/plain";
					}
					// Si el formato no coincide con .txt la api de google drive detecta el formato
					// automaticamente

					fileMetadata.setName(this.getName());
					fileMetadata.setParents(Collections.singletonList(project.getId()));

					java.io.File filePath = new java.io.File(IdeasRepo.get().getObjectFullUri(this));
					FileContent mediaContent = new FileContent(type, filePath);

					// Comprobamos que no se crea un fichero con el mismo nombre
					if (DriveQuickstart.getFilesByFolderId(project.getId()).contains(fileMetadata.getName())) {
						LOGGER.log(Level.INFO, "File " + this.getName() + " already exist");

					} else {
						// Guardamos el fichero
						com.google.api.services.drive.model.File f = DriveQuickstart.driveService().files()
								.create(fileMetadata, mediaContent).setFields("id").execute();

					}
					res = true;

				}
			}
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (ObjectClassNotValidException e) {

			LOGGER.log(Level.INFO, "Failed getting full path.");
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
			String id = DriveQuickstart
					.getFileByName(this.getName(), this.getProject(), this.getWorkspace(), this.getOwner()).getId();
			OutputStream outputStream = new ByteArrayOutputStream();
			DriveQuickstart.driveService().files().get(id).executeMediaAndDownloadTo(outputStream);
			res = res + outputStream;
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;

	}

	@Override
	protected byte[] readAsBytesImpl() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean exist() {
		boolean res = false;
		try {
			if (DriveQuickstart.getFileByName(this.getName(), this.getProject(), this.getWorkspace(),
					this.getOwner()) != null) {
				res = true;
			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}

		return res;
	}

}
