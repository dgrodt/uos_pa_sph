package main;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.lwjgl.opengl.Display;

import sph.SPH;

public class SettingsFrame extends JFrame implements WindowListener {
	private JTextField m_settingsField;
	private JTextField rho_settingsField;
	private JTextField c_settingsField;
	private JTextField gamma_settingsField;
	final private String[] settingsNames = { "Variable", "Value" };
	final private Object[][] settingsData = { { "m", new Float(0) },
			{ "rho", new Float(0) },
			{ "c", new Float(0) },
			{ "gamma", new Float(0) }, };

	public SettingsFrame() {
		setTitle("Settings");
		setSize(300, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		this.getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		settingsData[0][1] = new Float(SPH.getInstance().get_m());
		settingsData[1][1] = new Float(SPH.getInstance().get_rho());
		// m_settingsField = new JTextField(sph.get_m()+"");
		// rho_settingsField = new JTextField(sph.get_rho()+"");
		// c_settingsField = new JTextField(sph.get_c()+"");
		// gamma_settingsField = new JTextField(sph.get_gamma()+"");
		AbstractTableModel tableModel = new AbstractTableModel() {

			@Override
			public int getColumnCount() {
				return settingsNames.length;
			}

			@Override
			public int getRowCount() {
				return settingsData.length;
			}

			@Override
			public Object getValueAt(int arg0, int arg1) {
				return settingsData[arg0][arg1];
			}
			public Class getColumnClass(int c) {
		        return getValueAt(0, c).getClass();
		    }
			public boolean isCellEditable(int row, int col) {
		        return col > 0;
		    }
			public void setValueAt(Object value, int row, int col) {
		        settingsData[row][col] = value;
		        fireTableCellUpdated(row, col);
		    }
		};
		
		JTable table = new JTable(tableModel);
		table.setDefaultRenderer(Float.class, new DefaultTableCellRenderer() {
			public void setValue(Object value) {
				if(value == null) {
					setText("");
				}
				if(value instanceof Float) {
					setText(new BigDecimal(((Float) value).floatValue()).toPlainString());
				}
		    }
		});
		this.getContentPane().add(table);
		
		this.getContentPane().add(new JSeparator(SwingConstants.HORIZONTAL));

		// Buttons
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		final JButton setButton = new JButton("Save");
		JButton closeButton = new JButton("Close");
		JButton restartButton = new JButton("Restart");
		buttonPane.add(setButton);
		buttonPane.add(Box.createHorizontalStrut(50));
		buttonPane.add(closeButton);
		buttonPane.add(Box.createHorizontalStrut(5));
		//buttonPane.add(restartButton);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(buttonPane);
		
		tableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent arg0) {
				setButton.setText("Save*");
			}
			
		});
		setButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SPH.getInstance().setPause(true);
				System.out.println("------------------");
				SPH.getInstance().set_m((Float)settingsData[0][1]);
				SPH.getInstance().set_rho((Float)settingsData[1][1]);
				SPH.getInstance().set_c((Float)settingsData[2][1]);
				System.out.println("------------------");
				SPH.getInstance().setPause(false);
				setButton.setText("Save");
			}
			
		});
		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					SPH.destroy();
					// sph.close();
				} catch (Exception e) {
					// e.printStackTrace();
				}
				System.exit(0);
			}
			
		});
		restartButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					
					SPH.destroy();
					SPH.getInstance().init();
					setButton.doClick();
					SPH.getInstance().run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent arg0) {

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		try {
			SPH.destroy();
			// sph.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}
}
