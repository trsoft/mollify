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
	
	require_once("ShareHandler.class.php");
	
	class Share extends PluginBase {
		private $handler;
		
		public function version() {
			return "1_0";
		}

		public function versionHistory() {
			return array("1_0");
		}
		
		public function setup() {
			$this->addService("comment", "CommentServices");
			
			$this->handler = new ShareHandler($this->env);
			$this->env->events()->register("share/", $this->handler);

			$this->env->filesystem()->registerItemContextPlugin("share", $this);
		}
				
		public function getHandler() {
			return $this->handler;
		}
				
		public function __toString() {
			return "SharePlugin";
		}
	}
?>