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
	
	require_once("dao/ShareDao.class.php");
	
	class ShareHandler {
		private $env;
		private $settings;
		
		public function __construct($env, $settings) {
			$this->env = $env;
			$this->settings = $settings;
		}
				
		public function getItemContextData($item, $details, $key, $data) {
			return array(
				"count" => $this->dao()->getShareCount($item, $this->env->authentication()->getUserId())
			);
		}
		
		public function getShares($item) {
			return $this->dao()->getShares($item, $this->env->authentication()->getUserId());
		}

		public function addShare($item) {
			$this->dao()->addShare($this->GUID(), $item, $this->env->authentication()->getUserId(), time());
		}
		
		public function processShareGet($id) {
			$share = $this->dao()->getShare($id);
			if (!$share) throw new ServiceException("INVALID_REQUEST");
			// TODO check validity
			
			$this->env->filesystem()->allowFilesystems = TRUE;
			$item = $this->env->filesystem()->item($share["item_id"]);
			if (!$item) throw new ServiceException("INVALID_REQUEST");

			if ($item->isFile()) $this->processDownload($item);
			else $this->processUploadPage($id, $item);
		}
		
		private function processDownload($file) {
			$mobile = ($this->env->request()->hasParam("m") and strcmp($this->env->request()->param("m"), "1") == 0);
			
			$this->env->filesystem()->temporaryItemPermission($file, Authentication::PERMISSION_VALUE_READONLY);
			$this->env->filesystem()->download($file, $mobile);
		}

		private function processUploadPage($shareId, $folder) {
			$uploader = $this->getUploader();
			$uploader->showPage($shareId, $folder);
			die();
		}
		
		public function processSharePost($id) {
			$share = $this->dao()->getShare($id);
			if (!$share) throw new ServiceException("INVALID_REQUEST");
			// TODO check validity
			
			$this->env->filesystem()->allowFilesystems = TRUE;
			$item = $this->env->filesystem()->item($share["item_id"]);
			if (!$item or $item->isFile()) throw new ServiceException("INVALID_REQUEST");

			$this->processUpload($id, $item);
		}
				
		private function getUploader() {
			$uploader = "http";
			if (isset($this->settings) and isset($this->settings["uploader"])) $uploader = $this->settings["uploader"];
			
			require_once("upload/".$uploader."/PublicUploader.class.php");
			return new PublicUploader($this->env);
		}

		public function processUpload($shareId, $folder) {
			$this->env->filesystem()->temporaryItemPermission($folder, Authentication::PERMISSION_VALUE_READWRITE);
			$uploader = $this->getUploader();
			$uploader->uploadTo($shareId, $folder);
		}
								
		public function onEvent($e) {
			if (strcmp(FilesystemController::EVENT_TYPE_FILE, $e->type()) != 0) return;
			$type = $e->subType();
			
			if ($type === FileEvent::DELETE)
				$this->dao()->deleteShares($e->item());
		}
		
		private function dao() {
			return new ShareDao($this->env);
		}
		
		private function GUID() {
			if (function_exists('com_create_guid') === true)
				return str_replace('-', '', trim(com_create_guid(), '{}'));
			return sprintf('%04X%04X%04X%04X%04X%04X%04X%04X', mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(16384, 20479), mt_rand(32768, 49151), mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(0, 65535));
		}
				
		public function __toString() {
			return "CommentHandler";
		}
	}
?>