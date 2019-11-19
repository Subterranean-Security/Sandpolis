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

/// A view with a simple grid pattern for the login screen
class BackgroundView: UIView {

	/// The size of each grid square in pixels
	let gridSize = 16

	/// The width of the grid lines in pixels
	let lineWidth = 2

	override func draw(_ rect: CGRect) {
		let bg = UIBezierPath()
		bg.move(to: CGPoint(x: CGFloat(bounds.width / 2), y: 0))
		bg.addLine(to: CGPoint(x: CGFloat(bounds.width / 2), y: bounds.height))

		bg.close()
		UIColor(named: "grid/background")!.setStroke()
		bg.lineWidth = bounds.width
		bg.stroke()

		let grid = UIBezierPath()
		for i in 1...(Int(bounds.width) / gridSize) {
			grid.move(to: CGPoint(x: CGFloat(i * gridSize), y: 0))
			grid.addLine(to: CGPoint(x: CGFloat(i * gridSize), y: bounds.height))
		}

		for i in 1...(Int(bounds.height) / gridSize) {
			grid.move(to: CGPoint(x: 0, y: CGFloat(i * gridSize)))
			grid.addLine(to: CGPoint(x: bounds.width, y: CGFloat(i * gridSize)))
		}

		grid.close()
		UIColor(named: "grid/foreground")!.setStroke()
		grid.lineWidth = CGFloat(lineWidth)
		grid.stroke()
	}
}
