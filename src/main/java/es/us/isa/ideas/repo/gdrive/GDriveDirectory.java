package es.us.isa.ideas.repo.gdrive;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import es.us.isa.ideas.repo.Directory;
import es.us.isa.ideas.repo.IdeasRepo;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.impl.fs.FSNode;
import es.us.isa.ideas.repo.impl.fs.FSNodeIcon;
import es.us.isa.ideas.repo.operation.Listable;

public class GDriveDirectory extends Directory {
	private static final Logger LOGGER = Logger.getLogger(GDriveDirectory.class.getName());
	private Drive credentials;

	public GDriveDirectory(String name, String workspace, String project, String owner, Drive credentials) {
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
	protected boolean moveImpl(Listable dest, boolean copy) {
		boolean res = false;
		try {
			File directory = DriveQuickstart.getDirectoryByName(this.getName(), this.getProject(), this.getWorkspace(),
					this.getOwner(), this.credentials);
			if (directory == null) {
				LOGGER.log(Level.INFO, "Directory " + this.getName() + " does not exist.");

			} else {
				// Comprobamos que la carpeta destino sea un proyecto
				if (!(dest instanceof GDriveProject)) {
					LOGGER.log(Level.INFO, "The target folder must be a project");
				} else {
					System.out.println(IdeasRepo.get().getObjectFullUri(dest));
					String[] s1 = IdeasRepo.get().getObjectFullUri(dest).split("//");
					String[] s2 = s1[1].split("/");
					File project = DriveQuickstart.getProjectByName(s2[s2.length - 1], s2[s2.length - 2],
							s2[s2.length - 3], this.credentials);
					if (project == null) {
						LOGGER.log(Level.INFO, "Project " + dest.getClass().getName() + " does not exist.");

					} else {
						// Comprobar que directory no este en el proyecto destino
						File directoryInProject = DriveQuickstart.getDirectoryByName(this.getName(), project.getName(),
								this.getWorkspace(), this.getOwner(), this.credentials);
						if (directoryInProject == null) {
							// En este caso solo se permite mover carpetas
							DriveQuickstart.moveFileToFolder(directory.getId(), project.getId(), credentials);
							res = true;
						} else {
							LOGGER.log(Level.INFO, "Cannot move a folder with the same name.");

						}
					}
				}
			}
		} catch (IOException | GeneralSecurityException | ObjectClassNotValidException
				| ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	protected boolean deleteImpl() {
		boolean res = false;
		// Obtenemos la carpeta
		try {
			com.google.api.services.drive.model.File file = DriveQuickstart.getDirectoryByName(this.getName(),
					this.getProject(), this.getWorkspace(), this.getOwner(), this.credentials);
			if (file == null) {
				LOGGER.log(Level.INFO, "Directory " + this.getName() + " does not exist.");

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
		try {
			// Obtener el workspace donde se va a guardar el archivo
			com.google.api.services.drive.model.File workspace = DriveQuickstart.getWorkspaceByName(this.getWorkspace(),
					this.getOwner(), this.credentials);
			// Comprobar que el workspace existe
			if (workspace == null) {
				LOGGER.log(Level.INFO, "Workspace " + this.getWorkspace() + " does not exist.");

			} else {
				// Obtener el proyecto donde se va a guardar el directorio
				com.google.api.services.drive.model.File project = DriveQuickstart.getProjectByName(this.getProject(),
						this.getWorkspace(), this.getOwner(), this.credentials);
				// Comprobar que el proyecto existe
				if (project == null) {
					LOGGER.log(Level.INFO, "Project " + this.getProject() + " does not exist.");

				} else {
					// Comprobamos que no exista un directorio con el mismo nombre
					com.google.api.services.drive.model.File directory = DriveQuickstart.getDirectoryByName(
							this.getName(), this.getProject(), this.getWorkspace(), this.getOwner(),
							this.getCredentials());
					if (directory != null) {
						LOGGER.log(Level.INFO, "Directory " + this.getName() + " already exist");
					} else {
						// Creamos el fichero
						com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();

						fileMetadata.setMimeType("application/vnd.google-apps.folder");

						fileMetadata.setName(this.getName());
						fileMetadata.setParents(Collections.singletonList(project.getId()));

						// Guardamos la carpeta
						this.credentials.files().create(fileMetadata).setFields("id").execute();
						res = true;
					}

				}
			

			}

		} catch (

		GeneralSecurityException e) {
			e.printStackTrace();

		} catch (IOException e) {

			LOGGER.log(Level.INFO, "Failed creating new directory. ");
			e.printStackTrace();
		}
		return res;
	}

	@Override
	protected FSNode listImpl() {
		// Crear un nodo de la carpeta
		FSNode res = new FSNode();
		res.setTitle(this.getName());
		res.setFolder(true);
		res.setIcon(FSNodeIcon.FOLDER);
		String path=this.getWorkspace()+"/"+this.getProject()+"/"+this.getName();
		res.setKeyPath(path);
		try {
			File folder = DriveQuickstart.getDirectoryByName(this.getName(), this.getProject(), this.getWorkspace(),
					this.getOwner(), this.credentials);
			if (folder == null) {
				LOGGER.log(Level.INFO, "Directory " + this.getName() + " does not exist.");
				res = null;

			} else {
				res.setChildren(GDriveNode.getChildren(res.getKeyPath(),res,folder.getId(), credentials));

			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;
	}

	public boolean exist() {
		boolean res = false;
		try {
			if (DriveQuickstart.getDirectoryByName(this.getName(), this.getProject(), this.getWorkspace(),
					this.getOwner(), this.credentials) != null) {
				res = true;
			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}

		return res;
	}

}
