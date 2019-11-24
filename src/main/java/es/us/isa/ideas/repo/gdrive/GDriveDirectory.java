package es.us.isa.ideas.repo.gdrive;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.services.drive.model.File;

import es.us.isa.ideas.repo.Directory;
import es.us.isa.ideas.repo.IdeasRepo;
import es.us.isa.ideas.repo.Node;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.impl.fs.FSNode;
import es.us.isa.ideas.repo.impl.fs.FSNodeIcon;
import es.us.isa.ideas.repo.operation.Listable;

public class GDriveDirectory extends Directory {
	private static final Logger LOGGER = Logger.getLogger(GDriveDirectory.class.getName());

	public GDriveDirectory(String name, String workspace, String project, String owner) {
		super(name, workspace, project, owner);
	}

	@Override
	protected boolean moveImpl(Listable dest, boolean copy) {
		boolean res = false;
		try {
			File directory = DriveQuickstart.getDirectoryByName(this.getName(), this.getProject(), this.getWorkspace(),
					this.getOwner());
			if (directory == null) {
				LOGGER.log(Level.INFO, "Directory " + this.getName() + " does not exist.");

			} else {
				//Comprobamos que la carpeta destino sea un proyecto
				if(!(dest instanceof GDriveProject)) {
					LOGGER.log(Level.INFO, "The target folder must be a project");
				}else {
				System.out.println(IdeasRepo.get().getObjectFullUri(dest));
				String[] s1 = IdeasRepo.get().getObjectFullUri(dest).split("//");
				String[] s2 = s1[1].split("/");
				File project = DriveQuickstart.getProjectByName(s2[s2.length - 1], s2[s2.length - 2],
						s2[s2.length - 3]);
				if (project == null) {
					LOGGER.log(Level.INFO, "Project " + dest.getClass().getName() + " does not exist.");

				} else {
					if (!DriveQuickstart.getFilesByFolderId(project.getId()).contains(directory.getName())) {
						// En este caso solo se permite mover carpetas
						DriveQuickstart.moveFileToFolder(directory.getId(), project.getId());
						res=true;
					} else {
						LOGGER.log(Level.INFO, "Cannot move a folder with the same name.");

					}
				}
			}
			}
		} catch (IOException | GeneralSecurityException|ObjectClassNotValidException e) {
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
					this.getProject(), this.getWorkspace(), this.getOwner());
			if (file == null) {
				LOGGER.log(Level.INFO, "Directory " + this.getName() + " does not exist.");

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

					fileMetadata.setMimeType("application/vnd.google-apps.folder");

					fileMetadata.setName(this.getName());
					fileMetadata.setParents(Collections.singletonList(project.getId()));

					// Comprobamos que no se crea una carpeta con el mismo nombre
					if (DriveQuickstart.getFilesByFolderId(project.getId()).contains(fileMetadata.getName())) {
						LOGGER.log(Level.INFO, "Directory " + this.getName() + " already exist");

					} else {
						// Guardamos la carpeta
						com.google.api.services.drive.model.File f = DriveQuickstart.driveService().files()
								.create(fileMetadata).setFields("id").execute();

					}
					res = true;

				}
			}
		} catch (GeneralSecurityException e) {
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
		List<Node> children = new ArrayList<>();
		try {
			File folder = DriveQuickstart.getDirectoryByName(this.getName(), this.getProject(), this.getWorkspace(),
					this.getOwner());
			if (folder == null) {
				LOGGER.log(Level.INFO, "Directory " + this.getName() + " does not exist.");
				res = null;

			} else {

				List<String> files = new ArrayList<>(DriveQuickstart.getFilesByFolderId(folder.getId()));
				// Los hijos del nodo son todos los ficheros de esa carpeta
				for (String s : files) {
					FSNode child = new FSNode();
					child.setTitle(s);
					children.add(child);
				}
				res.setChildren(children);

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
					this.getOwner()) != null) {
				res = true;
			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}

		return res;
	}
}
