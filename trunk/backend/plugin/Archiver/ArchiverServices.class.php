<?php

	/**
	 * Copyright (c) 2008- Samuli J�rvel�
	 *
	 * All rights reserved. This program and the accompanying materials
	 * are made available under the terms of the Eclipse Public License v1.0
	 * which accompanies this distribution, and is available at
	 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
	 * this entire header must remain intact.
	 */

	class ArchiverServices extends ServicesBase {
		protected function isValidPath($method, $path) {
			return count($path) > 0;
		}
		
		public function isAuthenticationRequired() {
			return TRUE;
		}
		
		//GET zipped
		
		public function processPost() {
			$action = $this->path[0];
			
			if (!in_array($action, array("extract", "compress", "pack"))) throw $this->invalidRequestException();
			
			if ($action === 'extract') $this->onExtract($this->path[1]);
			else if ($action === 'extract') $this->onCompress($this->path[1]);
			else $this->onPack();
		}
		
		private function onPack() {
			$itemIds = $this->data['items'];
			if (count($itemIds) < 1) throw $this->invalidRequestException();
		
			$items = array();
			foreach($itemIds as $id)
				$items[] = $this->item($id);

			$archiveId = $this->archiveManager()->storeArchive($items);
			$this->response()->success(array("id" => $archiveId));
		}
		
		private function onExtract($itemId) {
			$data = $this->request->data;
			$overwrite = isset($data['overwrite']) ? $data['overwrite'] : FALSE;
			$archive = $this->item($itemId);
			$this->env->filesystem()->assertRights($archive, Authentication::RIGHTS_READ, "extract");
			
			$parent = $archive->parent();
			$this->env->filesystem()->assertRights($parent, Authentication::RIGHTS_WRITE, "extract");
			
			$name = str_replace(".", "_", basename($archive->internalPath()));
			$target = $parent->internalPath().DIRECTORY_SEPARATOR.$name.DIRECTORY_SEPARATOR;
			
			if (file_exists($target)) {
				if (!$overwrite)
					throw new ServiceException("DIR_ALREADY_EXISTS", $target);
				$parent->folderWithName($name)->delete();
			}
			
			mkdir($target);
			
			$this->archiveManager()->extract($archive->internalPath(), $target);
			$this->response()->success(array());
		}

		private function onCompress($itemId) {
			$data = $this->request->data;
			$overwrite = isset($data['overwrite']) ? $data['overwrite'] : FALSE;

			$folder = $this->item($itemId);
			if ($folder->isFile()) throw $this->invalidRequestException();
			$this->env->filesystem()->assertRights($folder, Authentication::RIGHTS_READ, "compress");
			
			$parent = $folder->parent();
			$this->env->filesystem()->assertRights($parent, Authentication::RIGHTS_WRITE, "compress");
			
			$name = str_replace(".", "_", basename($folder->internalPath()));
			$target = $parent->internalPath().DIRECTORY_SEPARATOR.$name.".zip";
			
			if (file_exists($target)) {
				if (!$overwrite)
					throw new ServiceException("FILE_ALREADY_EXISTS", $target);
				$parent->fileWithName($name)->delete();
			}
			
			$this->archiveManager()->compress($folder, $target);
			$this->response()->success(array());
		}
		
		private function archiveManager() {
			return $this->env->plugins()->getPlugin("Archiver")->getArchiveManager();
		}
					
		public function __toString() {
			return "ArchiverServices";
		}
	}
?>