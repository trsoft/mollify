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
	
	require_once("CommentHandler.class.php");
	
	class Comment extends PluginBase {
		private $handler;
		
		public function isConfigurationSupported($type) {
			return $type === ConfigurationProvider::TYPE_DATABASE;
		}
		
		public function version() {
			return "1_0";
		}

		public function versionHistory() {
			return array("1_0");
		}

		public function setup() {
			$this->addService("comment", "CommentServices");
			
			$this->handler = new CommentHandler($this->env);
			$this->env->events()->register("filesystem/", $this->handler);
			$this->env->filesystem()->registerDetailsPlugin($this->handler);
		}
		
		public function getHandler() {
			return $this->handler;
		}
		
		public function getClientPlugin() {
			return "client/plugin.js";
		}
		
		public function __toString() {
			return "CommentPlugin";
		}
	}
?>