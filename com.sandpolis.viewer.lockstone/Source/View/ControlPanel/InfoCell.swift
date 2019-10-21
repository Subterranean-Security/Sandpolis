//****************************************************************************//
//                                                                            //
//                Copyright © 2015 - 2019 Subterranean Security               //
//                                                                            //
//  Licensed under the Apache License, Version 2.0 (the "License");           //
//  you may not use this file except in compliance with the License.          //
//  You may obtain a copy of the License at                                   //
//                                                                            //
//      http://www.apache.org/licenses/LICENSE-2.0                            //
//                                                                            //
//  Unless required by applicable law or agreed to in writing, software       //
//  distributed under the License is distributed on an "AS IS" BASIS,         //
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  //
//  See the License for the specific language governing permissions and       //
//  limitations under the License.                                            //
//                                                                            //
//****************************************************************************//
import UIKit

class InfoCell: UITableViewCell {

	@IBOutlet weak var title: UILabel!
	@IBOutlet weak var value: UILabel!
	@IBOutlet weak var progress: UIActivityIndicatorView!

	public func setAttribute(_ attribute: Attribute) {
		title.text = attribute.title
		if let value = attribute.value {
			self.value.text = value
			self.value.isHidden = false
			progress.isHidden = true
		} else {
			progress.isHidden = false
			self.value.isHidden = true
		}
	}
}
