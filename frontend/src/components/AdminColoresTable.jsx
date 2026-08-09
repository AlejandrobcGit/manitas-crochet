

import "./AdminColoresTable.css";

function AdminColoresTable({
    colores,
    onEditar,
    onEliminar
}) {


    return (
        <div className="admin-tabla-container">

            <table className="admin-tabla">

                <thead>
                    <tr>
                        <th>Color</th>
                        <th>Codigo</th>
                        <th>Preview</th>
                        <th>Acciones</th>
                    </tr>
                </thead>

                <tbody>

                    {colores.map(color => {

                        return (
                            <tr key={color.id}>
                                <td>{color.nombre}</td>
                                <td>{color.codigo}</td>
                                <td>
                                    <div
                                        className="admin-tabla__color-preview"
                                        style={{ backgroundColor: color.codigo }}
                                    />
                                </td>
                                <td>
                                    <div className="admin-tabla__acciones">

                                        <button className="btn-editar"
                                            onClick={() => onEditar(color)}
                                        >
                                            Editar
                                        </button>

                                        <button className="btn-eliminar"
                                            onClick={() => onEliminar(color.id)}
                                        >
                                            Eliminar
                                        </button>

                                    </div>
                                </td>

                            </tr>
                        );

                    })}
                </tbody>
            </table>
        </div>
    );
}

export default AdminColoresTable;