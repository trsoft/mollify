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
		
	class CoreFileDataProvider {
		private $env;
		
		public function __construct($env) {
			$this->env = $env;
		}
				
		public function getRequestData($parent, $items, $result, $key, $dataRequest) {
			$result = array();
			foreach($items as $i) {
				$result[$i->publicId()] = $this->env->formatTimestampInternal($i->lastModified());
			} 
			return $result;
		}
				
		public function __toString() {
			return "CoreFileDataProvider";
		}
	}
?>