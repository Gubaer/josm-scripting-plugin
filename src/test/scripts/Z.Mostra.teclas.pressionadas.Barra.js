"use strict";

const MainApplication = Java.type("org.openstreetmap.josm.gui.MainApplication");
const JDialog         = Java.type("javax.swing.JDialog");
const JLabel          = Java.type("javax.swing.JLabel");
const Timer           = Java.type("javax.swing.Timer");
const LineBorder      = Java.type("javax.swing.border.LineBorder");
const Color           = Java.type("java.awt.Color");
const Font            = Java.type("java.awt.Font");
const Toolkit         = Java.type("java.awt.Toolkit");
const GraphicsEnvironment = Java.type("java.awt.GraphicsEnvironment");
const AWTEvent        = Java.type("java.awt.AWTEvent");
const KeyEvent        = Java.type("java.awt.event.KeyEvent");
const MouseEvent      = Java.type("java.awt.event.MouseEvent");
const MouseAdapter    = Java.extend(Java.type("java.awt.event.MouseAdapter"));
const MouseMotionAdapter = Java.extend(Java.type("java.awt.event.MouseMotionAdapter"));
const AWTEventListener = Java.extend(Java.type("java.awt.event.AWTEventListener"));
const ActionListener  = Java.extend(Java.type("java.awt.event.ActionListener"));

(function() {
    const CENTER_ALIGN = 0;

    const v = {
        lmb: false, rmb: false, alt: false, ctrl: false, shift: false, tecla: "",
        listener: null,

        init: function() {
            this.dialog = new JDialog(MainApplication.getMainFrame(), false);
            this.dialog.setUndecorated(true);
            this.dialog.setSize(675, 50);
            this.dialog.setLayout(null);

            const screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            const posX = Math.floor((screen.width * 6 / 10) - (this.dialog.getWidth() / 2));
            const posY = Math.floor(screen.height - this.dialog.getHeight() - 70);
            this.dialog.setLocation(posX, posY);

            this.lblLmb = this.criarLabel(10, 60);
            this.lblRmb = this.criarLabel(80, 60);
            this.lblAlt = this.criarLabel(170, 60);
            this.lblCtrl = this.criarLabel(240, 60);
            this.lblShift = this.criarLabel(310, 60);
            this.lblTecla = this.criarLabel(400, 230);

            this.lblFechar = new JLabel("X");
            this.lblFechar.setHorizontalAlignment(CENTER_ALIGN);
            this.lblFechar.setBounds(650, 10, 15, 30);
            this.lblFechar.setFont(new Font("Arial", Font.BOLD, 20));
            this.lblFechar.setForeground(Color.RED);
            this.lblFechar.addMouseListener(new MouseAdapter({
                mouseClicked: (e) => { this.encerrar(); }
            }));
            this.dialog.add(this.lblFechar);

            this.timer = new Timer(1000, new ActionListener({
                actionPerformed: (e) => {
                    this.alt = false; this.ctrl = false; this.shift = false; this.tecla = "";
                    this.atualizarLabels();
                }
            }));
            this.timer.setRepeats(false);
            this.listener = new AWTEventListener({
                eventDispatched: (event) => { this.handleEvent(event); }
            });

            Toolkit.getDefaultToolkit().addAWTEventListener(
                this.listener,
                AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK
            );

            let pontoInicial = null;
            this.dialog.addMouseListener(new MouseAdapter({
                mousePressed: (e) => { pontoInicial = e.getPoint(); }
            }));
            this.dialog.addMouseMotionListener(new MouseMotionAdapter({
                mouseDragged: (e) => {
                    const ponto = e.getLocationOnScreen();
                    this.dialog.setLocation(
                        Math.floor(ponto.x - (pontoInicial ? pontoInicial.x : 0)),
                        Math.floor(ponto.y - (pontoInicial ? pontoInicial.y : 0))
                    );
                }
            }));

            this.atualizarLabels();
            this.dialog.setVisible(true);
        },

        encerrar: function() {
            if (this.timer) this.timer.stop();
            if (this.listener) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(this.listener);
            }
            this.dialog.dispose();
            this.listener = null;
        },

        criarLabel: function(x, largura) {
            const lbl = new JLabel("");
            lbl.setHorizontalAlignment(CENTER_ALIGN);
            lbl.setBounds(Math.floor(x || 0), 5, Math.floor(largura || 50), 40);
            lbl.setFont(new Font("Arial", Font.BOLD, 20));
            lbl.setBorder(new LineBorder(new Color(125, 125, 125), 2));
            lbl.setForeground(new Color(125, 125, 125));
            this.dialog.add(lbl);
            return lbl;
        },

        atualizarLabels: function() {
            this.atualizar(this.lblLmb, "<L", this.lmb);
            this.atualizar(this.lblRmb, "R>", this.rmb);
            this.atualizar(this.lblAlt, "Alt", this.alt);
            this.atualizar(this.lblCtrl, "Ctrl", this.ctrl);
            this.atualizar(this.lblShift, "Shift", this.shift);

            if (this.lblTecla) {
                this.lblTecla.setFont(new Font("Arial", Font.BOLD, this.tecla ? 28 : 20));
                this.atualizar(this.lblTecla, this.tecla || "", !!this.tecla);
            }
        },

        atualizar: function(lbl, texto, ativo) {
            if (!lbl) return;
            lbl.setText(texto || "");
            const cor = ativo ? Color.WHITE : new Color(125, 125, 125);
            lbl.setForeground(cor);
            lbl.setBorder(new LineBorder(cor, 2));
        },

        handleEvent: function(event) {
            if (!this.dialog || !this.dialog.isVisible()) return;

            if (event instanceof KeyEvent) {
                if (event.getID() === KeyEvent.KEY_PRESSED) {
                    this.alt = event.isAltDown();
                    this.ctrl = event.isControlDown();
                    this.shift = event.isShiftDown();
                    const code = event.getKeyCode();

                    if (code >= KeyEvent.VK_NUMPAD0 && code <= KeyEvent.VK_NUMPAD9) {
                        this.tecla = (code - KeyEvent.VK_NUMPAD0).toString();
                    } else if (code === KeyEvent.VK_DECIMAL) this.tecla = ".";
                    else if (code === KeyEvent.VK_ADD) this.tecla = "+";
                    else if (code === KeyEvent.VK_SUBTRACT) this.tecla = "-";
                    else if (code === KeyEvent.VK_MULTIPLY) this.tecla = "*";
                    else if (code === KeyEvent.VK_DIVIDE) this.tecla = "/";
                    else if (![KeyEvent.VK_ALT, KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT_GRAPH].includes(code)) {
                        this.tecla = KeyEvent.getKeyText(code);
                    } else {
                        this.tecla = "";
                    }
                    this.atualizarLabels();
                    if (this.timer) this.timer.restart();
                }
            } else if (event instanceof MouseEvent) {
                const id = event.getID();
                if (id === MouseEvent.MOUSE_PRESSED) {
                    if (event.getButton() === MouseEvent.BUTTON1) this.lmb = true;
                    else if (event.getButton() === MouseEvent.BUTTON3) this.rmb = true;
                } else if (id === MouseEvent.MOUSE_RELEASED) {
                    if (event.getButton() === MouseEvent.BUTTON1) this.lmb = false;
                    else if (event.getButton() === MouseEvent.BUTTON3) this.rmb = false;
                }
                this.atualizarLabels();
            }
        }
    };

    v.init();
})();
