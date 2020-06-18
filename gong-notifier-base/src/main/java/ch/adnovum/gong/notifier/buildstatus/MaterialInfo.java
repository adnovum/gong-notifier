package ch.adnovum.gong.notifier.buildstatus;

public class MaterialInfo {
	private MaterialType materialType;
	private String url;
	private RepoCoordinates repoCoordinates;
	private String revision;

	public static class RepoCoordinates {
		private final String project;
		private final String repo;

		public RepoCoordinates(String project, String repo) {
			this.project = project;
			this.repo = repo;
		}

		public String getProject() {
			return project;
		}

		public String getRepo() {
			return repo;
		}

		@Override
		public String toString() {
			return "RepoCoordinates{" +
					"project='" + project + '\'' +
					", repo='" + repo + '\'' +
					'}';
		}
	}

	public enum MaterialType {
		GIT,
		SCM
	}

	public MaterialType getMaterialType() {
		return materialType;
	}

	public void setMaterialType(MaterialType materialType) {
		this.materialType = materialType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public RepoCoordinates getRepoCoordinates() {
		return repoCoordinates;
	}

	public void setRepoCoordinates(RepoCoordinates repoCoordinates) {
		this.repoCoordinates = repoCoordinates;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}
}
