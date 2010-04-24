<?php

	/**
	 * Copyright (c) 2008- Samuli Järvelä
	 *
	 * All rights reserved. This program and the accompanying materials
	 * are made available under the terms of the Eclipse Public License v1.0
	 * which accompanies this distribution, and is available at
	 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
	 * this entire header must remain intact.
	 */

	class FileViewerController {
		private $plugin;
		private $previewers = array();
		private $viewers = array();
		
		private $viewEnabled;
		private $previewEnabled;
		
		public function __construct($plugin, $view, $preview) {
			$this->plugin = $plugin;
			$this->viewEnabled = $view;
			$this->previewEnabled = $preview;
			
			if ($this->viewEnabled) {
				$viewers = $this->getSetting(TRUE, "viewers");
				if ($viewers != NULL and is_array($viewers)) {
					foreach($viewers as $t => $list)
						$this->registerViewer($list, $t);
				}
				$this->plugin->env()->features()->addFeature("file_view");
			}
			if ($this->previewEnabled) {
				$previewers = $this->getSetting(FALSE, "previewers");
				if ($previewers != NULL and is_array($previewers)) {
					foreach($previewers as $t => $list)
						$this->registerPreviewer($list, $t);
				}
				$this->plugin->env()->features()->addFeature("file_preview");
			}
		}

		private function registerPreviewer($types, $cls) {
			foreach($types as $t)
				$this->previewers[$t] = $cls;
		}
				
		private function registerViewer($types, $cls) {
			foreach($types as $t)
				$this->viewers[$t] = $cls;
		}
		
		public function getItemDetails($item) {
			if (!$item->isFile()) return FALSE;
			$type = strtolower($item->extension());
			
			$result = array();
			if ($this->previewEnabled and $this->isPreviewAllowed($type)) {
				$previewer = $this->getPreviewer($type);
				$result["preview"] = $previewer->getUrl($item);
			}
			if ($this->viewEnabled and $this->isViewAllowed($type)) {
				$viewer = $this->getViewer($type);
				$result["view"] = $viewer->getInfo($item);
			}
			return $result;
		}

		private function isPreviewAllowed($type) {
			if (!array_key_exists($type, $this->previewers)) return false;
			$types = $this->getSetting(FALSE, "types");
			if ($types == NULL or count($types) == 0) return TRUE;
			return in_array($type, $this->splitTypes($types));
		}
				
		private function isViewAllowed($type) {
			if (!array_key_exists($type, $this->viewers)) return false;
			$types = $this->getSetting(TRUE, "types");
			if ($types == NULL or count($types) == 0) return TRUE;
			return in_array($type, $this->splitTypes($types));
		}
		
		private function getPreviewer($type) {
			$previewer = $this->previewers[$type];
			list($id, $cls) = split("/", $previewer, 2);
			
			require_once("previewers/PreviewerBase.class.php");
			require_once("previewers/".$id."/".$cls.".previewer.php");
			return new $cls($this, $id);
		}
				
		private function getViewer($type) {
			$viewer = $this->viewers[$type];
			list($id, $cls) = split("/", $viewer, 2);
			
			require_once("viewers/ViewerBase.class.php");
			require_once("viewers/FullDocumentViewer.class.php");
			require_once("viewers/EmbeddedContentViewer.class.php");
			require_once("viewers/".$id."/".$cls.".viewer.php");
			return new $cls($this, $id);
		}
		
		public function getPreview($item) {
			$type = strtolower($item->extension());
			$previewer = $this->getPreviewer($type);
			return $previewer->getPreview($item);
		}
		
		public function processDataRequest($item, $path) {
			$type = strtolower($item->extension());
			$viewer = $this->getViewer($type);
			$viewer->processDataRequest($item, $path);
		}
		
		public function getContentUrl($item, $session = FALSE) {
			$url = $this->plugin->env()->getServiceUrl("view", array($item->id(), "content"), TRUE);
			if ($session and $this->plugin->env()->session()->isActive()) {
				$s = $this->plugin->env()->session()->getSessionInfo();
				$url .= '/?session='.$s["session_id"];
			}
			return $url;
		}

		public function response() {
			return $this->plugin->env()->response();
		}

		public function request() {
			return $this->plugin->env()->request();
		}
		
		public function getViewServiceUrl($item, $p, $fullUrl = FALSE) {
			$path = array($item->id());
			if ($p != NULL) $path = array_merge($path, $p);
			return $this->getServiceUrl("view", $path, $fullUrl);
		}
				
		public function getServiceUrl($id, $path, $fullUrl = FALSE) {
			return $this->plugin->env()->getServiceUrl($id, $path, $fullUrl);
		}

		public function getResourceUrl($viewerId) {
			return $this->plugin->env()->getPluginUrl($this->plugin->id(), "viewers/".$viewerId."/resources");
		}

		public function getCommonResourcesUrl() {
			return $this->plugin->env()->getCommonResourcesUrl();
		}
		
		private function splitTypes($list) {
			$result = array();
			foreach (explode(",", $list) as $t)
				$result[] = strtolower(trim($t));
			return $result;
		}

		public function getViewerSettings($viewerId) {
			$s = $this->plugin->getSettings();
			if (!isset($s[$viewerId])) return array();
			return $s[$viewerId];
		}

		private function getSetting($view, $name) {
			$s = $this->plugin->getSetting($view ? "view_options" : "preview_options", array());
			if (!isset($s[$name])) return NULL;
			return $s[$name];
		}

		public function __toString() {
			return "FileViewerController";
		}
	}
?>