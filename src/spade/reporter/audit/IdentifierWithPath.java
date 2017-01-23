/*
 --------------------------------------------------------------------------------
 SPADE - Support for Provenance Auditing in Distributed Environments.
 Copyright (C) 2015 SRI International

 This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 --------------------------------------------------------------------------------
 */

package spade.reporter.audit;

import java.util.HashMap;
import java.util.Map;

/**
 * Just a utility class. Added because FileIdentity, NamePipeIdentity and UnixSocketIdentity all have the same implementation.
 * So they all extend this class
 */

public abstract class IdentifierWithPath extends ArtifactIdentifier{
	
	private String path;
    private String label;
	
	public IdentifierWithPath(String path){
		path = path.replace("//", "/");
		this.path = path;
		this.label = null;
	}
	
    public IdentifierWithPath(String path, String label){
		path = path.replace("//", "/");
		this.path = path;
		this.label = label;
	}

	@Override
	public Map<String, String> getAnnotationsMap(){
		Map<String, String> annotations = new HashMap<String, String>();
		annotations.put("path", path);
		return annotations;
	}
	
	public String getPath(){
		return path;
	}
	
	public abstract String getSubtype();
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode()) + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdentifierWithPath other = (IdentifierWithPath) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;

		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;

		return true;
	}
}
