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

	class FilesystemServices extends ServicesBase {
		protected function isValidPath($method, $path) {
			if (count($path) < 1 or count($path) > 3)
				return FALSE;
			return TRUE;
		}
		
		public function processGet() {
			if ($this->path[0] === 'upload') {
				$this->processGetUpload();
				return;
			}
			$item = $this->item($this->path[0]);
			if ($item->isFile())
				$this->processGetFile($item);
			else
				$this->processGetFolder($item);
		}

		public function processPut() {
			if ($this->path[0] === 'permissions') {
				$this->env->authentication()->assertAdmin();
				$this->response()->success($this->env->configuration()->updateItemPermissions($this->request->data));
				return;
			}
			
			$item = $this->item($this->path[0]);
			if ($item->isFile())
				$this->processPutFile($item);
			else
				$this->processPutFolder($item);
		}
		
		public function processPost() {
			if ($this->path[0] === 'items') {
				$this->processMultiItemAction();
				return;
			}
			$item = $this->item($this->path[0]);
			if ($item->isFile())
				$this->processPostFile($item);
			else
				$this->processPostFolder($item);
		}
		
		public function processDelete() {
			if (count($this->path) == 1) {
				$this->env->filesystem()->delete($this->item($this->path[0]));
				$this->response()->success(TRUE);
				return;
			}
			if (count($this->path) == 2 and $this->path[1] === 'description') {
				$this->env->filesystem()->removeDescription($this->item($this->path[0]));
				$this->response()->success(TRUE);
				return;
			}
			
			throw $this->invalidRequestException();
		}
		
		private function item($id, $convert = TRUE) {
			return $this->env->filesystem()->item(($convert ? $this->convertItemID($id) : $id));
		}
		
		private function convertItemId($id) {
			return strtr(urldecode($id), '-_,', '+/=');
		}
		
		private function processMultiItemAction() {
			if (count($this->path) != 1) throw invalidRequestException();
			$data = $this->request->data;
			if (!isset($data['action']) or !isset($data['items']) or count($data['items']) < 1) throw $this->invalidRequestException();

			$items = array();
			foreach($data['items'] as $id)
				$items[] = $this->item($id);
			
			switch($data['action']) {
				case 'copy':
					if (!isset($data['to'])) throw $this->invalidRequestException();
					$this->env->filesystem()->copyItems($items, $this->item($data['to']));
					$this->response()->success(TRUE);
					return;
				case 'move':
					if (!isset($data['to'])) throw $this->invalidRequestException();
					$this->env->filesystem()->moveItems($items, $this->item($data['to']));
					$this->response()->success(TRUE);
					return;
				case 'delete':
					$this->env->filesystem()->deleteItems($items);
					$this->response()->success(TRUE);
					return;
				default:
					throw $this->invalidRequestException();
			}

		}
				
		private function processGetFile($item) {
			if (count($this->path) == 1) {
				$this->env->filesystem()->download($item);
				return;
			}
						
			switch (strtolower($this->path[1])) {
				case 'zip':
					$this->env->filesystem()->downloadAsZip($item);
					return;
				case 'details':
					$this->response()->success($this->env->filesystem()->details($item));
					break;
				case 'permissions':
					$this->response()->success($this->env->filesystem()->allPermissions($item));
					break;
				default:
					throw $this->invalidRequestException();
			}
		}
		
		private function processPutFile($item) {
			if (count($this->path) != 2) throw invalidRequestException();
						
			switch (strtolower($this->path[1])) {
				case 'name':
					$data = $this->request->data;
					if (!isset($data['name'])) throw $this->invalidRequestException();
					$this->env->filesystem()->rename($item, $data['name']);
					$this->response()->success(TRUE);
					break;
				case 'description':
					$this->env->filesystem()->setDescription($item, $this->request->data);
					$this->response()->success(TRUE);
					break;
				default:
					throw $this->invalidRequestException();
			}
		}
		
		private function processPostFile($item) {
			if (count($this->path) != 2) throw $this->invalidRequestException();
			
			switch (strtolower($this->path[1])) {
				case 'move':
					$data = $this->request->data;
					if (!isset($data['id'])) throw $this->invalidRequestException();
					$this->env->filesystem()->move($item, $this->item($data['id'], FALSE));
					break;
				case 'copy':
					$data = $this->request->data;
					if (!isset($data['id'])) throw $this->invalidRequestException();
					$this->env->filesystem()->copy($item, $this->item($data['id'], FALSE));
					break;
				default:
					throw $this->invalidRequestException();
			}
			
			$this->response()->success(TRUE);
		}
				
		private function processGetFolder($item) {
			if (count($this->path) != 2) throw invalidRequestException();
			
			switch (strtolower($this->path[1])) {
				case 'zip':
					$this->env->filesystem()->downloadAsZip($item);
					return;
				case 'items':
					$this->response()->success(array(
						"folders" => $this->env->filesystem()->folders($item),
						"files" => $this->env->filesystem()->files($item))
					);
					break;
				case 'files':
					$this->response()->success($this->env->filesystem()->files($item));
					break;
				case 'folders':
					$this->response()->success($this->env->filesystem()->folders($item));
					break;
				case 'details':
					$this->response()->success($this->env->filesystem()->details($item));
					break;
				case 'permissions':
					$this->response()->success($this->env->filesystem()->allPermissions($item));
					break;
				default:
					throw $this->invalidRequestException();
			}
		}
		
		private function processPutFolder($item) {
			if (count($this->path) != 2) throw invalidRequestException();
						
			switch (strtolower($this->path[1])) {
				case 'name':
					$data = $this->request->data;
					if (!isset($data['name'])) throw $this->invalidRequestException();
					$this->env->filesystem()->rename($item, $data['name']);
					$this->response()->success(TRUE);
					break;
				case 'description':
					$this->env->filesystem()->setDescription($item, $this->request->data);
					$this->response()->success(TRUE);
					break;
				default:
					throw $this->invalidRequestException();
			}
		}
		
		private function processPostFolder($item) {
			if (count($this->path) != 2) throw $this->invalidRequestException();
			
			switch (strtolower($this->path[1])) {
				case 'files':
					$this->env->filesystem()->uploadTo($item);
					break;
				case 'folders':
					$data = $this->request->data;
					if (!isset($data['name'])) throw $this->invalidRequestException();
					$this->env->filesystem()->createFolder($item, $data['name']);
					break;
				case 'move':
					$data = $this->request->data;
					if (!isset($data['id'])) throw $this->invalidRequestException();
					$this->env->filesystem()->move($item, $this->item($data['id'], FALSE));
					break;
				default:
					throw $this->invalidRequestException();
			}
			$this->response()->success(TRUE);
		}
		
		private function processGetUpload() {
			if (count($this->path) != 3 or $this->path[2] != 'status') throw invalidRequestException();
			$this->env->features()->assertFeature("file_upload_progress");
			
			Logging::logDebug('upload status '.$this->path[1]);
			$this->response()->success(apc_fetch('upload_'.$this->path[1]));
		}
	}
?>