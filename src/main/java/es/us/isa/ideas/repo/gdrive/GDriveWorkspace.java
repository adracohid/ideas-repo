package es.us.isa.ideas.repo.gdrive;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import es.us.isa.ideas.repo.Workspace;
import es.us.isa.ideas.repo.exception.AuthenticationException;
import es.us.isa.ideas.repo.exception.BadUriException;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.impl.fs.FSNode;
import es.us.isa.ideas.repo.impl.fs.FSNodeIcon;
import es.us.isa.ideas.repo.operation.Listable;

public class GDriveWorkspace extends Workspace {
	private Drive credentials;
	private static final Logger LOGGER = Logger.getLogger(GDriveWorkspace.class.getName());

	public GDriveWorkspace(String name, String owner, Drive credentials) {
		super(name, owner);
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean deleteImpl() {
		boolean res = false;
		// Obtenemos el workspace por el nombre
		try {
			File workspace = DriveQuickstart.getWorkspaceByName(this.getName(), this.getOwner(), this.credentials);
			if (workspace == null) {

				LOGGER.log(Level.INFO, "Workspace " + this.getName() + " does not exist.");
			} else {
				this.credentials.files().delete(workspace.getId()).execute();
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
		// Primero revisa si esta la carpeta del repositorio
		try {
			checkOwnerFolder(this.credentials);
			//Comprueba si ya existe el workspace creado
			File gw = DriveQuickstart.getWorkspaceByName(this.getName(), this.getOwner(), this.credentials);

			if (gw != null) {
				LOGGER.log(Level.INFO, "Workspace already exist");
			} else {

				File fileMetadata = new File();
				fileMetadata.setName(this.getName());
				fileMetadata.setMimeType("application/vnd.google-apps.folder");
				// Cada workspace se guarda dentro de la carpeta repositorio
				fileMetadata.setParents(Collections
						.singletonList(DriveQuickstart.getRepoFolder(this.getOwner(), this.credentials).getId()));
				// Antes de guardar el workspace comprobamos que no existe otro con el mismop
				// nombre

				// Se pasan los credenciales directamente al crear el objeto
				this.credentials.files().create(fileMetadata).setFields("id").execute();

				res = true;
			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;
	}

	private void checkOwnerFolder(Drive credentials) throws GeneralSecurityException, IOException {
		// Si no hay ninguna carpeta crea la carpeta del repositorio
		if (DriveQuickstart.getRepoFolder(this.getOwner(), this.credentials) == null) {
			File fileMetadata = new File();
			fileMetadata.setName(this.getOwner());
			fileMetadata.setMimeType("application/vnd.google-apps.folder");
			if (credentials == null) {
				DriveQuickstart.driveService().files().create(fileMetadata).setFields("id").execute();
			} else {
				credentials.files().create(fileMetadata).setFields("id").execute();
			}
		}

	}

	@Override
	protected FSNode listImpl() {
		// Buscar un workspace por el nombre (this.getName())
		// Crea un nodo de ese workspace
		FSNode res = new FSNode();
		res.setTitle(this.getName());
		res.setFolder(true);
		res.setIcon(FSNodeIcon.WORKSPACE);
		res.setKeyPath(this.getName());
		try {
			File workspace = DriveQuickstart.getWorkspaceByName(this.getName(), this.getOwner(), this.credentials);
			if (workspace == null) {
				LOGGER.log(Level.INFO, "Workspace " + this.getName() + " does not exist.");
				res = null;
			} else {
				res.setChildren(GDriveNode.getChildren(res.getTitle(),res,workspace.getId(), credentials));
			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public boolean exist() {
		boolean res = false;
		try {
			if (DriveQuickstart.getWorkspaceByName(this.getName(), this.getOwner(), this.credentials) != null) {
				res = true;
			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;
	}
	public boolean downloadWorkspace() {
		boolean res=false;
		try {
			DriveQuickstart.downloadWorkspace(this.getName(), this.getOwner(),this.credentials);
			res=true;

		} catch (IOException | GeneralSecurityException | AuthenticationException | ObjectClassNotValidException | BadUriException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

}
