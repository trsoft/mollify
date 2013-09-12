<?php

	/**
	 * CoreFileDataProvider.class.php
	 *
	 * Copyright 2008- Samuli Järvelä
	 * Released under GPL License.
	 *
	 * License: http://www.mollify.org/license.php
	 */
		
	class CoreFileDataProvider {
		private $env;
		
		public function __construct($env) {
			$this->env = $env;
		}
		
		public function init($c) {
			$c->registerDataRequestPlugin(array("core-file-modified", "core-item-description", "core-parent-description"), $this);
		}
				
		public function getRequestData($parent, $items, $result, $key, $requestData) {
			$result = array();
			if (strcmp("core-file-modified", $key) === 0) {
				foreach($items as $i) {
					$result[$i->id()] = $this->env->configuration()->formatTimestampInternal($i->lastModified());
				
				}
			} else if (strcmp("core-item-description", $key) === 0) {
				$result = $this->env->configuration()->findItemsWithDescription($parent);
			} else if (strcmp("core-parent-description", $key) === 0) {
				$result = $this->env->configuration()->getItemDescription($parent);
			} 
			
			return $result;
		}
				
		public function __toString() {
			return "CoreFileDataProvider";
		}
	}
?>